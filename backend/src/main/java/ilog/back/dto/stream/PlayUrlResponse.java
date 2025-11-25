package ilog.back.dto.stream;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayUrlResponse {
    private String hlsUrl;
    private String whepUrl;
    private String webrtcTestUrl; // 개발 디버그용(운영에서 숨길 수 있음)
}
