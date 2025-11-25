package ilog.back.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ilog.back.diary.entity.diary.Diary;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    List<Diary> findByGroupId(Long groupId);

    @Query("""
                select d from Diary d
                where d.createdAt >= :start and d.createdAt < :end
                  and not exists (select 1 from SentimentAnalysis sa where sa.diary.id = d.id)
            """)
    List<Diary> findAllNeedAnalysis(LocalDateTime start, LocalDateTime end);

}
