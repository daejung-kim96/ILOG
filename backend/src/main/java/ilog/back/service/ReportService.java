package ilog.back.service;

import ilog.back.dto.ReportDetailDTO;
import ilog.back.dto.ReportListItemDTO;
import ilog.back.entity.ReportType;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.YearMonth;

public interface ReportService {

    void buildWeekly(Long userId, LocalDate start, LocalDate end);
    void buildMonthly(Long userId, YearMonth ym);

    Page<ReportListItemDTO> list(Long userId, ReportType type, int page, int size);
    ReportDetailDTO detail(Long userId, Long reportId);
    ReportDetailDTO latest(Long userId, ReportType type);
}

