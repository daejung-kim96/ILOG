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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sentiment_analysis",
        uniqueConstraints = @UniqueConstraint(name = "uk_sentiment_diary", columnNames = "diary_id"))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SentimentAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Diary diary;

    @Column(nullable = false)
    private Short angry;

    @Column(nullable = false)
    private Short sad;

    @Column(nullable = false)
    private Short anxious;

    @Column(nullable = false)
    private Short hurt;

    @Column(nullable = false)
    private Short embarrass;

    @Column(nullable = false)
    private Short happy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String source = "local";

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal confidence;

    @Column(name = "model_version", nullable = false, length = 64)
    @Builder.Default
    private String modelVersion = "unknown";

    @Column(name = "llm_model", nullable = false, length = 64)
    private String llmModel;

    @Column(name = "content_hash", nullable = false, columnDefinition = "CHAR(64)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String contentHash;

    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public void apply(
            java.util.Map<String, Double> dist, String summary,
            java.math.BigDecimal confidence, String modelVer, String llmModel,
            String source, String contentHash, java.time.LocalDateTime analyzedAt
    ) {
        this.angry      = toPct(dist.get("angry"));
        this.sad        = toPct(dist.get("sad"));
        this.anxious    = toPct(dist.get("anxious"));
        this.hurt       = toPct(dist.get("hurt"));
        this.embarrass  = toPct(dist.get("embarrass"));
        this.happy      = toPct(dist.get("happy"));
        this.summary = summary;
        this.confidence = confidence;
        this.modelVersion = modelVer;
        this.llmModel = llmModel;
        this.source = source;
        this.contentHash = contentHash;
        this.analyzedAt = analyzedAt;
    }
    private short toPct(Double v){ return (short)Math.round(((v==null)?0.0:v)*100); }

}