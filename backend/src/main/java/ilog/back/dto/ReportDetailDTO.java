package ilog.back.dto;

import ilog.back.entity.ReportType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public record ReportDetailDTO(
        Long id,
        ReportType type,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime createdAt,
        Map<String, Object> content   // JSON 파싱 결과(유연)
) {}