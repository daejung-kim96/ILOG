package ilog.back.repository;

import ilog.back.entity.SentimentAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SentimentAnalysisRepository extends JpaRepository<SentimentAnalysis,Long> {

    // 조회용
    Optional<SentimentAnalysis> findByDiaryId(Long diaryId);

    // ===== 기간 평균 집계 =====
    public interface EmotionAgg {
        Double getAngryAvg();
        Double getSadAvg();
        Double getAnxiousAvg();
        Double getHurtAvg();
        Double getEmbarrassAvg();
        Double getHappyAvg();
        Long   getCnt();
    }

    @Query("""
        select
          avg(sa.angry)       as angryAvg,
          avg(sa.sad)         as sadAvg,
          avg(sa.anxious)     as anxiousAvg,
          avg(sa.hurt)        as hurtAvg,
          avg(sa.embarrass)   as embarrassAvg,
          avg(sa.happy)       as happyAvg,
          count(sa)           as cnt
        from SentimentAnalysis sa
        where sa.diary.userId = :userId
          and sa.analyzedAt >= :start
          and sa.analyzedAt <  :end
    """)
    EmotionAgg aggregateByUserAndPeriod(Long userId, LocalDateTime start, LocalDateTime end);

    // ===== 일별 평균 집계 =====
    public interface DailyAgg {
        LocalDate getDay();
        Double getAngry(); Double getSad(); Double getAnxious();
        Double getHurt();  Double getEmbarrass(); Double getHappy();
        Long   getCnt();
    }

    // MySQL/H2: FUNCTION('date', ...),  PostgreSQL이면 date_trunc('day', ...)로 바꿔야 함
    @Query("""
        select FUNCTION('date', sa.analyzedAt) as day,
               avg(sa.angry) as angry, avg(sa.sad) as sad, avg(sa.anxious) as anxious,
               avg(sa.hurt)  as hurt,  avg(sa.embarrass) as embarrass, avg(sa.happy) as happy,
               count(sa) as cnt
        from SentimentAnalysis sa
        where sa.diary.userId = :userId
          and sa.analyzedAt >= :start and sa.analyzedAt < :end
        group by FUNCTION('date', sa.analyzedAt)
        order by FUNCTION('date', sa.analyzedAt) asc
    """)
    List<DailyAgg> aggregateDaily(Long userId, LocalDateTime start, LocalDateTime end);

    // ===== 전기간 평균(전주/전월 비교용) =====
    public interface PeriodAvg {
        Double getAngry(); Double getSad(); Double getAnxious();
        Double getHurt();  Double getEmbarrass(); Double getHappy();
        Long   getCnt();
    }

    @Query("""
        select avg(sa.angry) as angry, avg(sa.sad) as sad, avg(sa.anxious) as anxious,
               avg(sa.hurt)  as hurt,  avg(sa.embarrass) as embarrass, avg(sa.happy) as happy,
               count(sa)     as cnt
        from SentimentAnalysis sa
        where sa.diary.userId = :userId
          and sa.analyzedAt >= :start and sa.analyzedAt < :end
    """)
    PeriodAvg aggregatePeriod(Long userId, LocalDateTime start, LocalDateTime end);

    // (옵션) 요약문 모으기
    public interface DaySummary {
        String getSummary();
        LocalDateTime getAnalyzedAt();
    }

    @Query("""
      select sa.summary as summary, sa.analyzedAt as analyzedAt
      from SentimentAnalysis sa
      where sa.diary.userId = :userId
        and sa.analyzedAt >= :start
        and sa.analyzedAt <  :end
      order by sa.analyzedAt asc
    """)
    List<DaySummary> findSummariesOfDay(Long userId, LocalDateTime start, LocalDateTime end);
}
