package ilog.back.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record SentimentDayDTO(
        Long userId,
        LocalDate date,
        long diaryCount,
        Map<String, Double> avg,
        String note,
        List<String> summaries
) {}