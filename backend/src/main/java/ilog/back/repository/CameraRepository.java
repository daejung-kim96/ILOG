package ilog.back.repository;

import ilog.back.entity.Camera;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CameraRepository extends JpaRepository<Camera, Long> {
    Page<Camera> findByOwnerUserId(Long ownerUserId, Pageable pageable);

    Page<Camera> findByOwnerUserIdAndNameContainingIgnoreCase(Long ownerUserId, String name, Pageable pageable);

    Optional<Camera> findByIdAndOwnerUserId(Long id, Long ownerUserId);
}
