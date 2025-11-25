package ilog.back.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ilog.back.client.WorkerClient;
import ilog.back.config.MtxProperties;
import ilog.back.dto.camera.CameraConfigRequest;
import ilog.back.dto.camera.CameraCreateRequest;
import ilog.back.dto.camera.CameraResponse;
import ilog.back.dto.stream.PlayUrlResponse;
import ilog.back.entity.Camera;
import ilog.back.repository.CameraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class CameraService {

    private final CameraRepository cameraRepository;
    private final WorkerClient workerClient;
    private final MtxProperties mtx;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static double clamp01(Double v) {
        if (v == null) return 0.0d;
        return Math.max(0d, Math.min(1d, v));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> ensureRule(Map<String, Object> rulesRoot, String key) {
        Object existing = rulesRoot.get(key);
        if (existing instanceof Map<?, ?> m) {
            return (Map<String, Object>) m;
        }
        Map<String, Object> child = new HashMap<>();
        rulesRoot.put(key, child);
        return child;
    }

    @Transactional
    public Long create(CameraCreateRequest req, Long ownerUserId) {
        if (ownerUserId == null) {
            throw new IllegalArgumentException("owner header is required");
        }
        var cam = Camera.builder()
                .name(req.getName())
                .rtspUrl(req.getRtspUrl())
                .streamName(req.getStreamName())
                .ownerUserId(ownerUserId)
                .build();
        cameraRepository.save(cam);
        return cam.getId();
    }

    @Transactional(readOnly = true)
    public Page<Camera> list(Long ownerUserId, String q, Pageable pageable) {
        if (ownerUserId == null) throw new IllegalArgumentException("owner header is required");
        if (q == null || q.isBlank()) return cameraRepository.findByOwnerUserId(ownerUserId, pageable);
        return cameraRepository.findByOwnerUserIdAndNameContainingIgnoreCase(ownerUserId, q.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public CameraResponse get(Long id, Long ownerUserId) {
        var c = loadOwned(id, ownerUserId);
        return CameraResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .rtspUrl(c.getRtspUrl())
                .streamName(c.getStreamName())
                .ownerUserId(c.getOwnerUserId())
                .armed(c.isArmed())
                .sensitivity(c.getSensitivity())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .configJson(c.getConfigJson())
                .build();
    }

    @Transactional
    public void saveConfig(Long cameraId, Long ownerUserId, CameraConfigRequest req) {
        var cam = loadOwned(cameraId, ownerUserId);

        // persist raw config as JSON
        cam = updateConfigJson(cam, req);

        // also persist streamName column if provided (used by /play)
        if (req.getStreamName() != null && !req.getStreamName().isBlank()) {
            cam.applyStreamName(req.getStreamName());
        }

        if (req.getArmed() != null) {
            cam.setArmed(req.getArmed());
        }

        if (req.getSensitivity() != null) {
            cam.setSensitivity(req.getSensitivity());
        }

        cameraRepository.save(cam);

        // propagate to worker
        Map<String, Object> payload = new HashMap<>();
        payload.put("camera_id", String.valueOf(cameraId));
        if (req.getRoi() != null) payload.put("roi", req.getRoi());
        if (req.getRecordOn() != null) payload.put("record_on", req.getRecordOn());
        if (req.getPostSec() != null) payload.put("post_sec", req.getPostSec());
        if (req.getArmed() != null) payload.put("armed", req.getArmed());

        // global motion sensitivity (MOTION)
        if (req.getSensitivity() != null) {
            double ui = cam.getSensitivity(); // 0..1 (low~high)
            // Map UI 0..1 -> motion threshold 0.15..0.03
            // 0   -> 0.15 (가장 둔감, 큰 움직임만)
            // 0.5 -> 0.09
            // 1   -> 0.03 (가장 민감)
            double maxThr = 0.15; // insensitive (needs big motion)
            double minThr = 0.03; // sensitive (small motion)
            double thr = maxThr - ui * (maxThr - minThr);
            payload.put("sensitivity", thr);
        }

        // per-rule tuning merged with raw rules from request (if any)
        Map<String, Object> rulesOverride = null;
        if (req.getRules() != null) {
            rulesOverride = new HashMap<>(req.getRules());
        }

        // WAKE_UP sensitivity slider
        if (req.getWakeUpSensitivity() != null) {
            if (rulesOverride == null) rulesOverride = new HashMap<>();
            double ui = clamp01(req.getWakeUpSensitivity());
            double minTh = 0.03d; // most sensitive
            double maxTh = 0.12d; // least sensitive
            double motionTh = maxTh - ui * (maxTh - minTh);
            Map<String, Object> wcfg = ensureRule(rulesOverride, "WAKE_UP");
            wcfg.put("motion_th", motionTh);
        }

        // SUDDEN_MOVE sensitivity slider
        if (req.getSuddenMoveSensitivity() != null) {
            if (rulesOverride == null) rulesOverride = new HashMap<>();
            double ui = clamp01(req.getSuddenMoveSensitivity());
            double minDelta = 0.03d;
            double maxDelta = 0.10d;
            double deltaTh = maxDelta - ui * (maxDelta - minDelta);
            double minImpulseRatio = 0.003d;
            double maxImpulseRatio = 0.02d;
            double impulseRatioTh = maxImpulseRatio - ui * (maxImpulseRatio - minImpulseRatio);
            double minTileRatio = 0.10d;
            double maxTileRatio = 0.40d;
            double tileRatioTh = maxTileRatio - ui * (maxTileRatio - minTileRatio);
            Map<String, Object> scfg = ensureRule(rulesOverride, "SUDDEN_MOVE");
            scfg.put("delta_th", deltaTh);
            scfg.put("impulse_ratio_th", impulseRatioTh);
            scfg.put("tile_ratio_th", tileRatioTh);
            // require_person는 워커 기본값(true)을 유지해서
            // 항상 "아이가 있을 때"만 SUDDEN_MOVE를 보도록 한다.
        }

        // LOUD sensitivity slider (audio)
        if (req.getLoudSensitivity() != null) {
            if (rulesOverride == null) rulesOverride = new HashMap<>();
            double ui = clamp01(req.getLoudSensitivity());
            double minRms = 0.04d;
            double maxRms = 0.15d;
            double rmsTh = maxRms - ui * (maxRms - minRms);
            double minBand = 0.20d;
            double maxBand = 0.50d;
            double bandTh = maxBand - ui * (maxBand - minBand);
            Map<String, Object> lcfg = ensureRule(rulesOverride, "LOUD");
            lcfg.put("rms_th", rmsTh);
            lcfg.put("band_energy_th", bandTh);
        }

        // OBJECT_NEAR_CHILD sensitivity slider (tracker-based)
        if (req.getObjectNearSensitivity() != null) {
            if (rulesOverride == null) rulesOverride = new HashMap<>();
            double ui = clamp01(req.getObjectNearSensitivity());
            // higher ui -> lower thresholds -> more sensitive
            double minSpeed = 4.0d;  // most sensitive
            double maxSpeed = 12.0d; // least sensitive
            double speedTh = maxSpeed - ui * (maxSpeed - minSpeed);
            double minDec = 5.0d;    // most sensitive
            double maxDec = 35.0d;   // least sensitive
            double distDecTh = maxDec - ui * (maxDec - minDec);
            Map<String, Object> ocfg = ensureRule(rulesOverride, "OBJECT_NEAR_CHILD");
            ocfg.put("speed_thresh", speedTh);
            ocfg.put("dist_dec_thresh", distDecTh);
        }

        if (rulesOverride != null) {
            payload.put("rules", rulesOverride);
        }

        // stream_name helps gateway mapping if needed
        String stream = (req.getStreamName() != null && !req.getStreamName().isBlank())
                ? req.getStreamName()
                : cam.effectiveStreamName();
        payload.put("stream_name", stream);

        workerClient.putConfig(payload);
    }

    @Transactional(readOnly = true)
    public PlayUrlResponse buildPlayUrls(Long cameraId, Long ownerUserId) {
        var cam = loadOwned(cameraId, ownerUserId);

        String stream = mtx.getDefaultStreamName();
        if (stream == null || stream.isBlank()) {
            stream = cam.effectiveStreamName();
        }
        String encStream = URLEncoder.encode(stream, StandardCharsets.UTF_8);

        String base = trimTrailingSlash(mtx.getPublicHost());
        String hls = base + replaceStream(mtx.getHlsTemplate(), encStream);
        String whep = base + replaceStream(mtx.getWhepTemplate(), encStream);
        String test = base + replaceStream(mtx.getTestTemplate(), encStream);
        return new PlayUrlResponse(hls, whep, test);
    }

    private String replaceStream(String template, String stream) {
        if (template == null) return "/app/" + stream + "/index.m3u8";
        return template.replace("{stream}", stream);
    }

    private String trimTrailingSlash(String s) {
        if (s == null) return "";
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }

    private Camera updateConfigJson(Camera cam, CameraConfigRequest req) {
        Map<String, Object> raw = new HashMap<>();
        if (req.getRoi() != null) raw.put("roi", req.getRoi());
        if (req.getRules() != null) raw.put("rules", req.getRules());
        if (req.getRecordOn() != null) raw.put("recordOn", req.getRecordOn());
        if (req.getPostSec() != null) raw.put("postSec", req.getPostSec());
        if (req.getStreamName() != null) raw.put("streamName", req.getStreamName());
        if (req.getArmed() != null) raw.put("armed", req.getArmed());
        if (req.getSensitivity() != null) raw.put("sensitivity", req.getSensitivity());
        if (req.getWakeUpSensitivity() != null) raw.put("wakeUpSensitivity", req.getWakeUpSensitivity());
        if (req.getSuddenMoveSensitivity() != null) raw.put("suddenMoveSensitivity", req.getSuddenMoveSensitivity());
        if (req.getLoudSensitivity() != null) raw.put("loudSensitivity", req.getLoudSensitivity());
        if (req.getObjectNearSensitivity() != null) raw.put("objectNearSensitivity", req.getObjectNearSensitivity());
        try {
            String json = objectMapper.writeValueAsString(raw);
            cam.applyConfigJson(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return cam;
    }

    @Transactional
    public void updateArmed(Long cameraId, Long ownerUserId, boolean armed) {
        var cam = loadOwned(cameraId, ownerUserId);
        cam.setArmed(armed);
        cameraRepository.save(cam);
    }

    private Camera loadOwned(Long id, Long ownerUserId) {
        if (ownerUserId == null) throw new IllegalArgumentException("owner header is required");
        return cameraRepository.findByIdAndOwnerUserId(id, ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("camera not found: " + id));
    }
}
