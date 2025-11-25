package ilog.back.controller;

import ilog.back.dto.ReportDetailDTO;
import ilog.back.dto.ReportListItemDTO;
import ilog.back.entity.ReportType;
import ilog.back.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 리스트: /api/reports?type=WEEKLY&page=0&size=10&userId=1
    @GetMapping
    public Page<ReportListItemDTO> list(
            @RequestParam Long userId,
            @RequestParam(required = false) ReportType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return reportService.list(userId, type, page, size);
    }

    // 상세: /api/reports/{id}?userId=1
    @GetMapping("/{id}")
    public ReportDetailDTO detail(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return reportService.detail(userId, id);
    }

    // 최신: /api/reports/latest?type=MONTHLY&userId=1
    @GetMapping("/latest")
    public ReportDetailDTO latest(
            @RequestParam Long userId,
            @RequestParam ReportType type) {
        return reportService.latest(userId, type);
    }
}
