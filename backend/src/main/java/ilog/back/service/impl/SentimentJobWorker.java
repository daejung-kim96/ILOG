package ilog.back.service.impl;

import ilog.back.entity.*;
import ilog.back.repository.*;
import ilog.back.diary.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SentimentJobWorker {

    private final DiaryRepository diaryRepo;
    private final SentimentJobRepository jobRepo;
    private final SentimentAnalysisRepository saRepo;
    private final EmotionAiClientImpl ai;

    // 하루치 일기 중 분석 필요 대상 큐잉
    @Transactional
    public void enqueueForDay(LocalDate day) {
        var s = day.atStartOfDay();
        var e = s.plusDays(1);
        var diaries = diaryRepo.findAllNeedAnalysis(s, e);

        for (var d : diaries) {
            boolean busy = jobRepo.existsByDiaryIdAndStatusIn(
                    d.getId(), List.of(Jobstatus.PENDING, Jobstatus.RUNNING));
            if (busy) continue;

            jobRepo.save(SentimentJob.builder()
                    .diary(d)
                    .contentHash(sha256(d.getContent()))
                    .status(Jobstatus.PENDING)
                    .build());
        }
    }

    //대기중 잡을 batchSize 만큼 처리하고 처리 건수 반환
    @Transactional
    public int runPending(int batchSize) {
        var jobs = jobRepo.findTop100ByStatusOrderByCreatedAtAsc(Jobstatus.PENDING)
                .stream().limit(batchSize).toList();

        for (var job : jobs) {
            job.markRunning();
            try {
                var d   = job.getDiary();
                var res = ai.analyze(d.getContent());

                var sa = saRepo.findByDiaryId(d.getId())
                        .orElseGet(() -> SentimentAnalysis.builder().diary(d).build());

                sa.apply(
                        res.distribution(), res.summary(),
                        BigDecimal.valueOf(res.confidence()),
                        res.modelVersion(), res.llmModel(), res.source(),
                        sha256(d.getContent()), LocalDateTime.now()
                );
                saRepo.save(sa);

                job.markDone(sa);
            } catch (Exception ex) {
                job.retryOrFail(crop(ex.toString()), 3); // 최대 3회 재시도
            }
        }
        return jobs.size();
    }

    // ===== 유틸 =====
    private String sha256(String s){ return DigestUtils.sha256Hex(s == null ? "" : s); }
    private String crop(String s){ return s == null ? null : (s.length() <= 2000 ? s : s.substring(0, 2000)); }
}
