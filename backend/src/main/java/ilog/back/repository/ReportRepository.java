package ilog.back.repository;

import ilog.back.entity.Report;
import ilog.back.entity.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // ===== 업서트/중복 방지 =====
    boolean existsByUser_IdAndTypeAndStartDateAndEndDate(
            Long userId, ReportType type, LocalDate startDate, LocalDate endDate);

    Optional<Report> findByUser_IdAndTypeAndStartDateAndEndDate(
            Long userId, ReportType type, LocalDate startDate, LocalDate endDate);

    // ===== 목록/페이지 =====

    // 목록 조회
    Page<Report> findByUser_IdOrderByStartDateDesc(Long userId, Pageable pageable);

    // 타입 필터 + 페이지
    Page<Report> findByUser_IdAndTypeOrderByStartDateDesc(Long userId, ReportType type, Pageable pageable);

    // ===== 단건 =====
    Optional<Report> findByIdAndUser_Id(Long id, Long userId);

    // 최신 1건 -> 메인페이지에 쓸용도
    Optional<Report> findFirstByUser_IdAndTypeOrderByStartDateDesc(Long userId, ReportType type);

    // ===== 정정/재생성 시 삭제 =====
    long deleteByUser_IdAndTypeAndStartDateAndEndDate(
            Long userId, ReportType type, LocalDate startDate, LocalDate endDate);

}
