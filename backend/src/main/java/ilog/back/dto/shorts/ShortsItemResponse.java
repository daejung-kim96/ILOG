package ilog.back.dto.shorts;

import ilog.back.entity.ShortsStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortsItemResponse {
    private Long id;
    private Long cameraId;
    private Long sourceEventId;
    private ShortsStatus status;
    private String clipUrl;
    private String thumbUrl;
    private BigDecimal durationSec;
    private LocalDateTime createdAt;
}

