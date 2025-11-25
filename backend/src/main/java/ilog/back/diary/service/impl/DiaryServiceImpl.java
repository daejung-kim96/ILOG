package ilog.back.diary.service.impl;

import ilog.back.diary.dto.diary.AttachmentDto;
import ilog.back.diary.dto.diary.DiaryCreateRequest;
import ilog.back.diary.dto.diary.DiaryResponseDto;
import ilog.back.diary.entity.diary.Attachment;
import ilog.back.diary.entity.diary.Diary;
import ilog.back.diary.entity.diary.MediaType;
import ilog.back.diary.repository.AttachmentRepository;
import ilog.back.diary.service.DiaryService;
import ilog.back.diary.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import ilog.back.diary.repository.DiaryRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DiaryServiceImpl implements DiaryService {

    private final DiaryRepository diaryRepository;

    private final AttachmentRepository attachmentRepository;
    private final S3Service s3Service;

    @Override
    @Transactional // 쓰기 작업이므로 Transactional 필수
    public void createDiary(String userId, DiaryCreateRequest request,
            List<MultipartFile> images, List<MultipartFile> videos) {

        // 1. 일기(Diary) 본문 저장
        Diary diary = Diary.builder()
                .userId(Long.parseLong(userId)) // String userId를 Long으로 변환 (필요시)
                .userName(request.userName())
                .groupId(request.groupId())
                .title(request.title())
                .content(request.content())
                .status(request.status())
                .build();

        diaryRepository.save(diary);

        // 2. 이미지 업로드 및 저장 처리
        uploadAndSaveAttachments(diary, images, MediaType.IMAGE);

        // 3. 동영상 업로드 및 저장 처리
        uploadAndSaveAttachments(diary, videos, MediaType.VIDEO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiaryResponseDto> getDiariesByGroupId(Long groupId, String userId) {
        List<Diary> diaries = diaryRepository.findByGroupId(groupId);
        Long currentUserId = Long.parseLong(userId);

        return diaries.stream()
                .filter(diary -> !diary.isPrivateDiary() || diary.getUserId().equals(currentUserId))
                .map(this::mapToDiaryResponseDto)
                .collect(Collectors.toList());
    }

    private DiaryResponseDto mapToDiaryResponseDto(Diary diary) {
        String[] themeColors = { "rose", "green", "amber" };
        String themeColor = themeColors[(int) (diary.getId() % 3)];

        List<AttachmentDto> images = diary.getAttachments().stream()
                .filter(a -> a.getType() == MediaType.IMAGE)
                .map(a -> AttachmentDto.builder()
                        .id(a.getId())
                        .type(a.getType())
                        .url(a.getUrl())
                        .build())
                .collect(Collectors.toList());

        List<AttachmentDto> videos = diary.getAttachments().stream()
                .filter(a -> a.getType() == MediaType.VIDEO)
                .map(a -> AttachmentDto.builder()
                        .id(a.getId())
                        .type(a.getType())
                        .url(a.getUrl())
                        .build())
                .collect(Collectors.toList());

        return DiaryResponseDto.builder()
                .id(diary.getId())
                .userId(diary.getUserId())
                .userName(diary.getUserName())
                .createdAt(diary.getCreatedAt())
                .title(diary.getTitle())
                .content(diary.getContent())
                .themeColor(themeColor)
                .status(diary.getStatus())
                .images(images)
                .videos(videos)
                .build();
    }

    /**
     * 반복되는 업로드 로직을 처리하는 헬퍼 메서드
     */
    private void uploadAndSaveAttachments(Diary diary, List<MultipartFile> files,
            MediaType type) {
        if (files == null || files.isEmpty()) {
            return;
        }

        List<Attachment> attachments = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty())
                continue;

            try {
                // ✅ S3Service를 통해 MinIO에 업로드하고 URL을 받아옵니다.
                String fileUrl = s3Service.uploadFile(file);

                // Attachment 엔티티 생성
                Attachment attachment = Attachment.builder()
                        .diary(diary) // 연관관계 매핑
                        .type(type) // IMAGE or VIDEO
                        .url(fileUrl) // S3(MinIO) URL
                        .originName(file.getOriginalFilename())
                        .build();

                attachments.add(attachment);

            } catch (IOException e) {
                // 🚨 중요: Checked Exception을 Unchecked로 감싸서 던져야 트랜잭션이 롤백됩니다.
                log.error("파일 업로드 실패: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
            }
        }

        // DB에 메타데이터 일괄 저장
        if (!attachments.isEmpty()) {
            attachmentRepository.saveAll(attachments);
        }
    }

}
