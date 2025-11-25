package ilog.back.entity;

import ilog.back.diary.entity.diary.Diary;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "sentiment_job")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SentimentJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diary_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Diary diary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sentiment_analysis_id" ,nullable = true)
    private SentimentAnalysis sentimentAnalysis;

    @Column(name = "content_hash", nullable = false, columnDefinition = "CHAR(64)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String contentHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private Jobstatus status = Jobstatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @PrePersist
    private void prePersist() {
        if (status == null) status = Jobstatus.PENDING;
        if (retryCount == null) retryCount = 0;
    }

    @Column(nullable = true, columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== 도메인 메서드 =====
    public void markRunning() {
        this.status = Jobstatus.RUNNING;
        this.lastError = null;
    }
    public void markDone(SentimentAnalysis sa) {
        this.sentimentAnalysis = sa;
        this.status = Jobstatus.DONE;
        this.lastError = null;
    }
    public void markFailed(String error) {
        this.status = Jobstatus.FAILED;
        this.lastError = error;
    }
    public void retryOrFail(String error, int maxRetries) {
        this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
        this.lastError = error;
        this.status = (this.retryCount >= maxRetries) ? Jobstatus.FAILED : Jobstatus.PENDING;
    }
}
