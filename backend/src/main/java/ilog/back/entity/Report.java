package ilog.back.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "report",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_report_user_type_range",
                        columnNames = {"user_id", "type", "start_date", "end_date"}
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private ReportType type;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void validateRange() {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must be >= startDate");
        }
    }

    // ===== 도메인 메서드 =====
    public void updateContent(String newContent) {
        if (newContent == null || newContent.isBlank())
            throw new IllegalArgumentException("content must not be blank");
        this.content = newContent;
    }

    // ==== 나중에 동일 기간 레포트를 작성할 일이 생길때 상정 ====
    public static Report ofRefUser(Long userId, ReportType type,
                                   LocalDate start, LocalDate end, String content) {
        return Report.builder()
                .user(User.builder().id(userId).build())  // 참조만
                .type(type)
                .startDate(start)
                .endDate(end)
                .content(content)
                .build();
    }
}
