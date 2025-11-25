package ilog.back.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SentimentJobCoordinator {

    private final SentimentJobWorker worker;

    // 전날 일기 큐잉
    public void enqueueYesterday() {
        worker.enqueueForDay(LocalDate.now().minusDays(1));
    }

    // 시연용 당일 일기 큐잉
    public void enqueueToday() {
        worker.enqueueForDay(LocalDate.now());
    }
    
    // 모든 대기 잡을 batchSize 단위로 다 돌릴 때
    public void runAllPending(int batchSize) {
        while (true) {
            int n = worker.runPending(batchSize);
            if (n == 0) break;
        }
    }
}
