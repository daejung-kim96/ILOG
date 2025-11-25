package ilog.back.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "mtx")
public class MtxProperties {
    /**
     * Public host that browsers use to access MediaMTX, e.g. https://stream.example.com
     */
    private String publicHost;

    /**
     * Path template for HLS playback. Use {stream} placeholder.
     * Default: /app/{stream}/index.m3u8
     */
    private String hlsTemplate = "/app/{stream}/index.m3u8";

    /**
     * Path template for WebRTC (WHEP). Use {stream} placeholder.
     * Default: /whep/{stream}
     */
    private String whepTemplate = "/whep/{stream}";

    /**
     * Optional test page path template; Use {stream} placeholder.
     * Default: /test-webrtc.html?s={stream}
     */
    private String testTemplate = "/test-webrtc.html?s={stream}";

    /**
     * Fixed MediaMTX path name when using a single camera (e.g. c200).
     * When set, backend will prefer this over per-camera streamName.
     */
    private String defaultStreamName = "c200";
}
