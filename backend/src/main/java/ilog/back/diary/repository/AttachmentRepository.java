package ilog.back.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ilog.back.diary.entity.diary.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

}
