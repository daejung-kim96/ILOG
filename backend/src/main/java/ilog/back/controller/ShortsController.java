package ilog.back.controller;

import ilog.back.dto.common.PageResponse;
import ilog.back.dto.shorts.ShortsItemResponse;
import ilog.back.entity.Shorts;
import ilog.back.entity.ShortsStatus;
import ilog.back.repository.ShortsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shorts")
@RequiredArgsConstructor
public class ShortsController {

    private final ShortsRepository shortsRepository;

    @GetMapping
    public ResponseEntity<PageResponse<ShortsItemResponse>> list(
            @RequestParam(required = false) Long cameraId,
            @RequestParam(required = false) ShortsStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Shorts> p;
        if (cameraId != null && status != null) {
            p = shortsRepository.findByCameraIdAndStatus(cameraId, status, pageable);
        } else if (cameraId != null) {
            p = shortsRepository.findByCameraId(cameraId, pageable);
        } else {
            p = shortsRepository.findAll(pageable);
        }
        List<ShortsItemResponse> items = p.getContent().stream().map(s -> ShortsItemResponse.builder()
                .id(s.getId())
                .cameraId(s.getCamera().getId())
                .sourceEventId(s.getSourceEvent() != null ? s.getSourceEvent().getId() : null)
                .status(s.getStatus())
                .clipUrl(s.getClipUrl())
                .thumbUrl(s.getThumbUrl())
                .durationSec(s.getDurationSec())
                .createdAt(s.getCreatedAt())
                .build()).toList();

        return ResponseEntity
                .ok(new PageResponse<>(items, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages()));
    }
}
