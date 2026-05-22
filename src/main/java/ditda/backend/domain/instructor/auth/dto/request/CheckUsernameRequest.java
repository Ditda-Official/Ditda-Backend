package ditda.backend.domain.instructor.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "유저 아이디 중복 확인 요청")
public record CheckUsernameRequest(

	@Schema(description = "유저 ID (6 ~ 20자 이내)", example = "testid123")
	@NotBlank(message = "아이디는 필수입니다.")
	@Size(min = 6, max = 20, message = "아이디는 6자 이상 20자 이하여야 합니다.")
	String username
) {
}
