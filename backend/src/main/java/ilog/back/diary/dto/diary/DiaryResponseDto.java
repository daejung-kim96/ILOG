package ilog.back.diary.dto.diary;

import ilog.back.diary.entity.diary.DiaryStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DiaryResponseDto {
    private Long id;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
    private String title;
    private String content;
    private String themeColor;
    private DiaryStatus status;
    private List<AttachmentDto> images;
    private List<AttachmentDto> videos;
}
