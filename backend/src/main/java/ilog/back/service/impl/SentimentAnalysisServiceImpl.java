package ilog.back.service.impl;

import ilog.back.dto.SentimentDayDTO;
import ilog.back.repository.SentimentAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// 시연용 감정분석 1일치 분석 JSON 반환

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SentimentAnalysisServiceImpl implements ilog.back.service.SentimentAnalysisService {

    private final SentimentAnalysisRepository saRepo;

    @Override
    public SentimentDayDTO getDaySummary(Long userId, LocalDate day) {
        var start = day.atStartOfDay();
        var end   = start.plusDays(1);

        var agg = saRepo.aggregateByUserAndPeriod(userId, start, end);
        if (agg == null || agg.getCnt() == 0) {
            return new SentimentDayDTO(userId, day, 0, Map.of(), "데이터 없음", List.of());
        }

        var avgMap = Map.of(
                "angry",      nz(agg.getAngryAvg()),
                "sad",        nz(agg.getSadAvg()),
                "anxious",    nz(agg.getAnxiousAvg()),
                "hurt",       nz(agg.getHurtAvg()),
                "embarrass",  nz(agg.getEmbarrassAvg()),
                "happy",      nz(agg.getHappyAvg())
        );

        var summaries = saRepo.findSummariesOfDay(userId, start, end).stream()
                .limit(3)
                .map(SentimentAnalysisRepository.DaySummary::getSummary)
                .toList();

        return new SentimentDayDTO(userId, day, agg.getCnt(), avgMap, agg.getCnt() + "건 집계", summaries);
    }

    private static double nz(Double v){ return v == null ? 0d : v; }
}
