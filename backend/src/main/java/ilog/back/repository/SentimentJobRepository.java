package ilog.back.repository;

import ilog.back.entity.Jobstatus;
import ilog.back.entity.SentimentJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SentimentJobRepository extends JpaRepository<SentimentJob, Long> {

    // 지금 작업중인지
    boolean existsByDiaryIdAndStatusIn(Long diaryId, Collection<Jobstatus> statuses);
   
    // 목록 조회
    List<SentimentJob> findTop100ByStatusOrderByCreatedAtAsc(Jobstatus status);
}
