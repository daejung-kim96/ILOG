package ilog.back.diary.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import ilog.back.diary.dto.diary.DiaryCreateRequest;
import ilog.back.diary.dto.diary.DiaryResponseDto;

public interface DiaryService {

    void createDiary(
            String userId,
            DiaryCreateRequest request,
            List<MultipartFile> images,
            List<MultipartFile> videos);

    List<DiaryResponseDto> getDiariesByGroupId(Long groupId, String userId);

}
