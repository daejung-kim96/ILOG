package ilog.back.repository;

import ilog.back.entity.Shorts;
import ilog.back.entity.ShortsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortsRepository extends JpaRepository<Shorts, Long> {
    Page<Shorts> findByCameraId(Long cameraId, Pageable pageable);
    Page<Shorts> findByCameraIdAndStatus(Long cameraId, ShortsStatus status, Pageable pageable);
}
