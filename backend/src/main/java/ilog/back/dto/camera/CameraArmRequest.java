package ilog.back.dto.camera;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CameraArmRequest {
    @NotNull
    private Boolean armed;
}

