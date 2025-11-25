package ilog.back.controller;

import ilog.back.dto.event.EventIngestRequest;
import ilog.back.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingest(@Valid @RequestBody EventIngestRequest req) {
        Long id = eventService.ingest(req);
        return ResponseEntity.created(URI.create("/api/events/" + id))
                .body(Map.of("eventId", id));
    }
}
