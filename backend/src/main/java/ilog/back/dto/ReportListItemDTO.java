package ilog.back.dto;

import ilog.back.entity.ReportType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public record ReportListItemDTO(
        Long id,
        ReportType type,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime createdAt
) {}