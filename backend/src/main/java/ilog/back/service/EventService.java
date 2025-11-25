package ilog.back.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ilog.back.dto.event.EventIngestRequest;
import ilog.back.entity.*;
import ilog.back.repository.CameraRepository;
import ilog.back.repository.EventRepository;
import ilog.back.repository.ShortsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventService {

    private final CameraRepository cameraRepository;
    private final EventRepository eventRepository;
    private final ShortsRepository shortsRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public Long ingest(EventIngestRequest req) {
        var cam = cameraRepository.findById(req.getCameraId())
                .orElseThrow(() -> new IllegalArgumentException("camera not found: " + req.getCameraId()));

        String payloadJson = null;
        try {
            if (req.getPayload() != null) payloadJson = objectMapper.writeValueAsString(req.getPayload());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (!cam.isArmed()) {
            return null;
        }

        var event = Event.builder()
                .camera(cam)
                .type(req.getType())
                .payloadJson(payloadJson)
                .clipUrl(req.getClip() != null ? req.getClip().getUrl() : null)
                .occurredAt(req.getOccurredAt() != null ? req.getOccurredAt() : LocalDateTime.now())
                .build();
        eventRepository.save(event);

        if (event.getClipUrl() != null && !event.getClipUrl().isBlank()) {
            var shorts = Shorts.builder()
                    .camera(cam)
                    .sourceEvent(event)
                    .status(ShortsStatus.READY)
                    .clipUrl(event.getClipUrl())
                    .build();
            shortsRepository.save(shorts);
        }
        return event.getId();
    }
}
