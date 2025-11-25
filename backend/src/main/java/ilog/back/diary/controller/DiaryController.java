package ilog.back.diary.controller;

import ilog.back.common.response.ApiResponse;
import ilog.back.diary.dto.diary.DiaryCreateRequest;
import ilog.back.diary.dto.diary.DiaryResponseDto;
import ilog.back.diary.service.DiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    /**
     * 일기 생성 (이미지, 영상 업로드 포함)
     * Content-Type: multipart/form-data
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> createDiary(
            @RequestHeader("X-User-Id") String userId, // 게이트웨이 등에서 넘어오는 유저 ID

            // 1. 텍스트 데이터 (FormData의 필드들을 객체로 매핑)
            @ModelAttribute @Valid DiaryCreateRequest request,

            // 2. 이미지 파일 리스트 (필수 아님 -> required = false)
            @RequestPart(value = "images", required = false) List<MultipartFile> images,

            // 3. 영상 파일 리스트 (필수 아님 -> required = false)
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos) {
        // 4. Facade 계층으로 데이터 전달 (파일이 null일 경우 빈 리스트 처리는 서비스 혹은 여기서 수행)
        diaryService.createDiary(
                userId,
                request,
                images,
                videos);

        return ApiResponse.of(201);
    }

    /**
     * 일기 목록 조회
     */
    @GetMapping
    public ApiResponse<List<DiaryResponseDto>> getDiaries(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam Long groupId) {
        return ApiResponse.of(200, diaryService.getDiariesByGroupId(groupId, userId));
    }

}
