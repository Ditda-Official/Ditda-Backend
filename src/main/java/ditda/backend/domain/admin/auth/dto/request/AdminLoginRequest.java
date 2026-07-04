package ditda.backend.domain.admin.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "어드민 로그인 요청")
public record AdminLoginRequest(

	@Schema(description = "로그인 ID", example = "admin")
	@NotBlank(message = "아이디는 필수입니다.")
	String username,

	@Schema(description = "비밀번호", example = "admin1234")
	@NotBlank(message = "비밀번호는 필수입니다.")
	String password
) {

}
