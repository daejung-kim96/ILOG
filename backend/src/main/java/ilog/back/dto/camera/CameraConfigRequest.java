package ilog.back.dto.camera;

import lombok.Data;
import java.util.Map;

@Data
public class CameraConfigRequest {
    // passthrough objects to Worker
    private Object roi;
    private Map<String, Object> rules;

    private Boolean recordOn;   // simple flag; worker may also accept list in future
    private Integer postSec;    // seconds to keep recording after event

    // optional: override stream name for playback/templates
    private String streamName;

    private Boolean armed;
    private Double sensitivity;

    // per-rule sensitivities (0..1, higher = more sensitive)
    private Double wakeUpSensitivity;
    private Double suddenMoveSensitivity;
    private Double loudSensitivity;
    private Double objectNearSensitivity;
}
