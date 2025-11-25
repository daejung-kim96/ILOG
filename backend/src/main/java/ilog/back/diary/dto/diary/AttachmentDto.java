package ilog.back.diary.dto.diary;

import ilog.back.diary.entity.diary.MediaType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttachmentDto {
    private Long id;
    private MediaType type;
    private String url;
}
