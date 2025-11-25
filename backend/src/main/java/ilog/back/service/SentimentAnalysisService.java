package ilog.back.service;

import ilog.back.dto.SentimentDayDTO;
import java.time.LocalDate;

public interface SentimentAnalysisService {

    SentimentDayDTO getDaySummary(Long userId, LocalDate day);
}