package ilog.back.diary.dto.diary;

import ilog.back.diary.entity.diary.DiaryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DiaryCreateRequest(
                @NotNull(message = "그룹 ID는 필수입니다.") Long groupId,

                @NotBlank(message = "작성자 이름은 필수입니다.") String userName,

                @NotBlank(message = "제목을 입력해주세요.") String title,

                @NotBlank(message = "내용을 입력해주세요.") String content,

                @NotNull(message = "공개 여부를 설정해주세요.") DiaryStatus status) {
}