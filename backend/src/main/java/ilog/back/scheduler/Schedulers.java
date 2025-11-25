package ilog.back.scheduler;

import ilog.back.repository.UserRepository;
import ilog.back.service.impl.SentimentJobCoordinator;
import ilog.back.service.impl.ReportServiceImpl; // 인터페이스 쓰면 ReportService로 바꿔도 OK
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class Schedulers {

    private final SentimentJobCoordinator jobCoordinator;
    private final ReportServiceImpl reportService;
    private final UserRepository userRepo;

    // 배치 사이즈(한 번에 처리할 잡 수) 설정값 주입
    @Value("${batch.analyze.batch-size:200}")
    private int batchSize;

    // ========== 1) 감정분석 일일 배치 ==========

    // 매일 3시 전날 일기 큐잉
    @Scheduled(cron = "${batch.analyze.enqueue-cron:0 0 3 * * *}", zone = "Asia/Seoul")
    public void enqueueDaily() {
        log.info("[ANALYZE][ENQUEUE] yesterday");
        jobCoordinator.enqueueYesterday();
    }

    // 매일 3시 5분 대기 잡 실행 (배치 사이즈 단위, 끝날 떄 까지)
    @Scheduled(cron = "${batch.analyze.run-cron:0 5 3 * * *}", zone = "Asia/Seoul")
    public void runJobs() {
        log.info("[ANALYZE][RUN] batchSize={}", batchSize);
        jobCoordinator.runAllPending(batchSize);
    }

    // ========== 2) 리포트: 주간/월간 ==========

    // 매주 월요일 4시에 주간 레포트 생성
    @Scheduled(cron = "${batch.report.weekly-cron:0 0 4 * * MON}", zone = "Asia/Seoul")
    public void weeklyReports() {
        LocalDate today = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate();
        LocalDate lastWeekStart = today.with(DayOfWeek.MONDAY).minusWeeks(1);
        LocalDate lastWeekEnd   = lastWeekStart.plusDays(6);

        var userIds = userRepo.findAllIds();
        log.info("[REPORT][WEEKLY] {}~{}, users={}", lastWeekStart, lastWeekEnd, userIds.size());

        for (Long userId : userIds) {
            reportService.buildWeekly(userId, lastWeekStart, lastWeekEnd);
        }
    }

    // 매월 1일 5시 월간 레포트 생성
    @Scheduled(cron = "${batch.report.monthly-cron:0 0 5 1 * *}", zone = "Asia/Seoul")
    public void monthlyReports() {
        YearMonth prev = YearMonth.from(ZonedDateTime.now(ZoneId.of("Asia/Seoul"))).minusMonths(1);

        var userIds = userRepo.findAllIds();
        log.info("[REPORT][MONTHLY] {} ({} ~ {}), users={}",
                prev, prev.atDay(1), prev.atEndOfMonth(), userIds.size());

        for (Long userId : userIds) {
            reportService.buildMonthly(userId, prev);
        }
    }
}
