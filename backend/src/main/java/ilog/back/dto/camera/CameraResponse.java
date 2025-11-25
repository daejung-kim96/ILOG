package ilog.back.dto.camera;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CameraResponse {
    private Long id;
    private String name;
    private String rtspUrl;
    private String streamName;
    private Long ownerUserId;
    private boolean armed;
    private double sensitivity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String configJson;
}
