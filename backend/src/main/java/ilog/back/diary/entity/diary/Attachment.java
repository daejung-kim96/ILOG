package ilog.back.diary.entity.diary;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id")
    private Diary diary;

    @Enumerated(EnumType.STRING)
    private MediaType type; // IMAGE, VIDEO

    private String url; // MinIO 저장 경로 (예: /bucket/diary/images/abc.jpg)

    private String originName; // 사용자가 올린 원래 파일명
}