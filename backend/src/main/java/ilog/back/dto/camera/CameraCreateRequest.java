package ilog.back.dto.camera;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CameraCreateRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String rtspUrl;

    // optional: streamName override; if null, use name
    private String streamName;
}

