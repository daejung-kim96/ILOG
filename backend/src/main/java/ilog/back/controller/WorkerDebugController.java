package ilog.back.controller;

import ilog.back.client.WorkerClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/worker")
@RequiredArgsConstructor
public class WorkerDebugController {

    private final WorkerClient workerClient;

    @GetMapping("/signals")
    public ResponseEntity<Map<String, Object>> signals() {
        Map<String, Object> body = workerClient.fetchSignals();
        return ResponseEntity.ok(body);
    }

    @GetMapping(value = "/preview", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> preview() {
        byte[] img = workerClient.fetchPreview();
        if (img == null || img.length == 0) {
            return ResponseEntity.status(503).build();
        }
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(img);
    }
}

