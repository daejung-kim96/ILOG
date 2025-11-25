package ilog.back.controller;

import ilog.back.dto.camera.CameraArmRequest;
import ilog.back.dto.camera.CameraConfigRequest;
import ilog.back.dto.camera.CameraCreateRequest;
import ilog.back.dto.camera.CameraResponse;
import ilog.back.dto.common.PageResponse;
import ilog.back.dto.stream.PlayUrlResponse;
import ilog.back.service.CameraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cameras")
@RequiredArgsConstructor
public class CameraController {

    private final CameraService cameraService;

    @GetMapping
    public ResponseEntity<PageResponse<CameraResponse>> list(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100));
        Page<ilog.back.entity.Camera> p = cameraService.list(userId, q, pageable);
        List<CameraResponse> items = p.getContent().stream().map(c -> CameraResponse.builder()
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
                .build()).toList();
        return ResponseEntity.ok(new PageResponse<>(items, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CameraResponse> get(@PathVariable Long id,
                                              @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(cameraService.get(id, userId));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CameraCreateRequest req
    ) {
        Long id = cameraService.create(req, userId);
        return ResponseEntity.created(URI.create("/api/cameras/" + id))
                .body(Map.of("cameraId", id));
    }

    @PutMapping("/{id}/config")
    public ResponseEntity<Void> saveConfig(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CameraConfigRequest req
    ) {
        cameraService.saveConfig(id, userId, req);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/play")
    public ResponseEntity<PlayUrlResponse> playUrls(@PathVariable Long id,
                                                    @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(cameraService.buildPlayUrls(id, userId));
    }

    // stubs for future operations
    @PostMapping("/{id}/resolve")
    public ResponseEntity<Void> resolve(@PathVariable Long id) {
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<Void> apply(@PathVariable Long id) {
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{id}/armed")
    public ResponseEntity<Void> setArmed(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CameraArmRequest req
    ) {
        cameraService.updateArmed(id, userId, req.getArmed());
        return ResponseEntity.noContent().build();
    }
}
