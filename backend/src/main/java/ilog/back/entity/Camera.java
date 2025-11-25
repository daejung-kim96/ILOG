package ilog.back.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "camera")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Camera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "rtsp_url", nullable = false, length = 1000)
    private String rtspUrl;

    // optional: override playback stream name; defaults to name
    @Column(name = "stream_name", length = 200)
    private String streamName;

    // Store last applied configuration JSON (roi/rules/recordOn/postSec)
    @Column(name = "config_json", columnDefinition = "TEXT")
    private String configJson;

    // owner id propagated from gateway header (no FK)
    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Builder.Default
    @Column(name = "armed", nullable = false)
    private boolean armed = true;

    @Builder.Default
    @Column(name = "sensitivity", nullable = false)
    private double sensitivity = 0.5d;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String effectiveStreamName() {
        return (streamName == null || streamName.isBlank()) ? name : streamName;
    }

    public void applyConfigJson(String json) {
        this.configJson = json;
    }

    public void applyStreamName(String newStreamName) {
        if (newStreamName != null && !newStreamName.isBlank()) {
            this.streamName = newStreamName;
        }
    }

    public void setArmed(boolean armed) {
        this.armed = armed;
    }

    public void setSensitivity(Double val) {
        if (val != null) {
            double clamped = Math.max(0d, Math.min(1d, val));
            this.sensitivity = clamped;
        }
    }
}
