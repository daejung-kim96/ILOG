package ilog.back.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ilog.back.dto.ReportDetailDTO;
import ilog.back.dto.ReportListItemDTO;
import ilog.back.entity.Report;
import ilog.back.entity.ReportType;
import ilog.back.repository.ReportRepository;
import ilog.back.repository.SentimentAnalysisRepository;
import ilog.back.service.ReportService;
import ilog.back.service.llm.LlmClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

//레포트 생성/조회 서비스

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final SentimentAnalysisRepository saRepo;
    private final ReportRepository reportRepo;
    private final ObjectMapper objectMapper;
    private final LlmClient llm; // ← [추가] LLM 요약 사용

    // 제네릭 정보를 보존하기 위한 TypeReference 상수
    private static final TypeReference<Map<String, Object>> MAP_S_O = new TypeReference<>() {};

    // 라벨 한글 매핑 (요약문에 사용)
    private static final Map<String, String> KO = Map.of(
            "angry", "분노", "sad", "슬픔", "anxious", "불안",
            "hurt", "상처", "embarrass", "머쓱함", "happy", "행복"
    );

    // ====== 공통 헬퍼 ======
    private ReportDetailDTO toDetailDTO(Report r) {
        // null-safe: content 정규화
        final String content = r.getContent() == null ? "" : r.getContent();

        Map<String, Object> parsed;
        try {
            if (content.isBlank()) {
                parsed = Map.of();                // 빈 내용이면 빈 맵
            } else {
                parsed = objectMapper.readValue(content, MAP_S_O);
            }
        } catch (Exception e) {
            // Map.of는 null 금지 → content는 위에서 이미 "" 로 정규화됨
            parsed = Map.of(
                    "raw", content,
                    "parse_error", e.getClass().getSimpleName()
            );
        }

        return new ReportDetailDTO(
                r.getId(), r.getType(), r.getStartDate(), r.getEndDate(), r.getCreatedAt(), parsed
        );
    }

    private Report getByIdOrThrow(Long userId, Long reportId) {
        return reportRepo.findByIdAndUser_Id(reportId, userId)
                .orElseThrow(() -> new IllegalArgumentException("report not found"));
    }

    private Report getLatestOrThrow(Long userId, ReportType type) {
        return reportRepo.findFirstByUser_IdAndTypeOrderByStartDateDesc(userId, type)
                .orElseThrow(() -> new IllegalArgumentException("report not found"));
    }

    // ====== 추세 분석: 선형회귀 기울기 기반 ======
    // daily: ReportRepository.aggregateDaily(...) 결과를 FE 친화적으로 Map 리스트로 전달받는다고 가정
    private Map<String, Object> analyzeTrend(Map<String, Double> avg,
                                             List<Map<String, Object>> daily,
                                             double slopePerDayThreshold) {
        // 지배 감정(평균 기준)
        String dominant = null;
        double best = -1;
        for (var e : avg.entrySet()) {
            if (e.getValue() != null && e.getValue() > best) {
                best = e.getValue();
                dominant = e.getKey();
            }
        }

        var rise = new ArrayList<String>();
        var fall = new ArrayList<String>();

        if (daily.size() >= 3) { // 최소 3포인트는 있어야 회귀 의미
            int n = daily.size();
            double sumX = 0, sumXX = 0;
            for (int i = 0; i < n; i++) { sumX += i; sumXX += i * (double) i; }
            double denom = n * sumXX - sumX * sumX; // 공통 분모

            if (denom != 0) {
                for (var k : KO.keySet()) {
                    double sumY = 0, sumXY = 0;
                    boolean ok = true;
                    for (int i = 0; i < n; i++) {
                        var vObj = daily.get(i).get(k);
                        if (vObj == null) { ok = false; break; }
                        double y = ((Number) vObj).doubleValue();
                        sumY += y;
                        sumXY += i * y;
                    }
                    if (!ok) continue;

                    // OLS slope = (N*Σ(xy) - Σx*Σy) / (N*Σ(x^2) - (Σx)^2)
                    double slope = (n * sumXY - sumX * sumY) / denom;

                    if (slope >= slopePerDayThreshold) rise.add(k);
                    else if (slope <= -slopePerDayThreshold) fall.add(k);
                }
            }
        }

        var m = new LinkedHashMap<String, Object>();
        m.put("dominant", dominant);
        m.put("rise", rise);
        m.put("fall", fall);
        // 참고용으로 임계치도 포함 → FE에 설명 가능
        m.put("slope_threshold_per_day", slopePerDayThreshold);
        return m;
    }

    // ====== 짧은 요약 생성 (한글) ======
    private String buildSummary(ReportType type,
                                LocalDate start, LocalDate end,
                                Map<String, Double> avg,
                                Map<String, Object> trend) {
        String dom = (String) trend.get("dominant");
        String domKo = dom != null ? KO.getOrDefault(dom, dom) : "특정 감정";
        String period = (type == ReportType.WEEKLY) ? "주간" : "월간";

        // 증가/감소 감정 문자열
        @SuppressWarnings("unchecked")
        List<String> r = (List<String>) trend.getOrDefault("rise", List.of());
        @SuppressWarnings("unchecked")
        List<String> f = (List<String>) trend.getOrDefault("fall", List.of());
        String riseStr = r.isEmpty() ? "증가 추세 없음" :
                String.join(", ", r.stream().map(k -> KO.getOrDefault(k, k)).toList());
        String fallStr = f.isEmpty() ? "감소 추세 없음" :
                String.join(", ", f.stream().map(k -> KO.getOrDefault(k, k)).toList());

        double happy = Optional.ofNullable(avg.get("happy")).orElse(0.0);
        String mood = (happy >= 0.4) ? "전반적으로 밝은 편"
                : (happy <= 0.2) ? "전반적으로 무거운 편" : "복합적";

        return "%s 보고서: %s~%s 기간에 %s 감정이 지배적이었습니다. 추세상 증가: [%s], 감소: [%s]. 전반 분위기는 %s입니다."
                .formatted(period, start, end, domKo, riseStr, fallStr, mood);
    }

    // ====== 일별 집계 DTO → Map 변환 ======
    private List<Map<String, Object>> toDailySeries(List<SentimentAnalysisRepository.DailyAgg> rows) {
        var list = new ArrayList<Map<String, Object>>();
        for (var r : rows) {
            var m = new LinkedHashMap<String, Object>();
            m.put("day", r.getDay());
            m.put("angry", safe(r.getAngry()));
            m.put("sad", safe(r.getSad()));
            m.put("anxious", safe(r.getAnxious()));
            m.put("hurt", safe(r.getHurt()));
            m.put("embarrass", safe(r.getEmbarrass()));
            m.put("happy", safe(r.getHappy()));
            m.put("count", r.getCnt());
            list.add(m);
        }
        return list;
    }

    private double safe(Double v) { return v == null ? 0.0 : v; }

    // ====== content JSON 생성 (ObjectMapper 사용) ======
    private String buildContentJson(
            ReportType type,
            LocalDate start, LocalDate end,
            Map<String, Double> avg,
            List<Map<String, Object>> daily,
            Map<String, Object> trend,
            long count,
            String summaryOverride // LLM 요약 주입
    ) {
        try {
            var root = new LinkedHashMap<String, Object>();
            root.put("type", type.name());
            root.put("start", start.toString());
            root.put("end", end.toString());
            root.put("avg", avg);
            root.put("daily", daily);
            root.put("trend", trend);

            String summary = (summaryOverride != null && !summaryOverride.isBlank())
                    ? summaryOverride
                    : buildSummary(type, start, end, avg, trend); // 기존 알고리즘 백업
            root.put("summary", summary);

            root.put("count", count);
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return """
               {"type":"%s","start":"%s","end":"%s","error":"%s"}
               """.formatted(type.name(), start, end, e.getClass().getSimpleName());
        }
    }

    // ======================= 공통 생성/업서트 진입점 =======================
    private void buildAndUpsert(Long userId,
                                ReportType type,
                                LocalDate start,
                                LocalDate end,
                                String periodKo,          // "주간"/"월간"
                                double slopePerDayThresh) // 주간 0.01, 월간 0.003
    {
        // 집계(기간 평균)
        var cur = saRepo.aggregateByUserAndPeriod(
                userId, start.atStartOfDay(), end.plusDays(1).atStartOfDay());
        if (cur == null || cur.getCnt() == 0) return;

        // 일별 집계 가져오기 → 추세/요약 생성에 사용
        var dailyRows = saRepo.aggregateDaily(
                userId, start.atStartOfDay(), end.plusDays(1).atStartOfDay());
        var daily = toDailySeries(dailyRows);

        // 평균 맵
        var avg = Map.of(
                "angry", safe(cur.getAngryAvg()),
                "sad", safe(cur.getSadAvg()),
                "anxious", safe(cur.getAnxiousAvg()),
                "hurt", safe(cur.getHurtAvg()),
                "embarrass", safe(cur.getEmbarrassAvg()),
                "happy", safe(cur.getHappyAvg())
        );

        // 추세 계산
        var trend = analyzeTrend(new LinkedHashMap<>(avg), daily, slopePerDayThresh);

        // LLM 호출 (요약 생성) — 실패 시 null → buildContentJson에서 알고리즘 요약으로 fallback
        String llmSummary = llm.summarizeReport(
                periodKo, start.toString(), end.toString(), avg, daily, trend
        );

        // content JSON 구성(요약/추세/일별 포함)
        var content = buildContentJson(
                type, start, end, avg, daily, trend, cur.getCnt(), llmSummary
        );

        // 업서트
        reportRepo.findByUser_IdAndTypeAndStartDateAndEndDate(userId, type, start, end)
                .ifPresentOrElse(
                        r -> r.updateContent(content),
                        () -> reportRepo.save(Report.ofRefUser(userId, type, start, end, content))
                );
    }

    // ====== 업서트(생성/갱신) ======
    @Transactional
    public void buildWeekly(Long userId, LocalDate start, LocalDate end) {
        // 주간 기울기 임계치: 하루당 ±0.01
        buildAndUpsert(userId, ReportType.WEEKLY, start, end, "주간", 0.01);
    }

    @Transactional
    public void buildMonthly(Long userId, YearMonth ym) {
        var start = ym.atDay(1);
        var end   = ym.atEndOfMonth();
        // 월간 기울기 임계치: 하루당 ±0.003 (≈ 한달 누적 ±0.09)
        buildAndUpsert(userId, ReportType.MONTHLY, start, end, "월간", 0.003);
    }

    // ====== 조회 ======
    @Transactional(readOnly = true)
    public Page<ReportListItemDTO> list(Long userId, ReportType type, int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        Page<Report> p = (type == null)
                ? reportRepo.findByUser_IdOrderByStartDateDesc(userId, pageable)
                : reportRepo.findByUser_IdAndTypeOrderByStartDateDesc(userId, type, pageable);

        return p.map(r -> new ReportListItemDTO(
                r.getId(), r.getType(), r.getStartDate(), r.getEndDate(), r.getCreatedAt()
        ));
    }

    @Transactional(readOnly = true)
    public ReportDetailDTO detail(Long userId, Long reportId) {
        return toDetailDTO(getByIdOrThrow(userId, reportId));
    }

    @Transactional(readOnly = true)
    public ReportDetailDTO latest(Long userId, ReportType type) {
        return toDetailDTO(getLatestOrThrow(userId, type));
    }
}
