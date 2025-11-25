package ilog.back.controller;

import ilog.back.dto.SentimentDayDTO;
import ilog.back.service.SentimentAnalysisService;
import ilog.back.service.impl.SentimentJobCoordinator;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

// 순수 시연용 감전분석 JSON 반환
@RestController
@RequestMapping("/api/v1/demo/sentiment")
@RequiredArgsConstructor
public class SentimentDemoController {

    private final SentimentAnalysisService sentimentAnalysisService;
    private final SentimentJobCoordinator sentimentJobCoordinator;

    // 일일 집계
    @GetMapping("/day")
    public SentimentDayDTO getDay(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return sentimentAnalysisService.getDaySummary(userId, date);
    }

    @PostMapping("/run-today")
    public String runToday(@RequestParam(defaultValue = "50") int batchSize) {
        sentimentJobCoordinator.enqueueToday();
        sentimentJobCoordinator.runAllPending(batchSize);
        return "OK";
    }
}
