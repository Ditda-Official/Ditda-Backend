package ditda.backend.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "인증번호 검증 요청")
public record EmailCodeVerificationRequest(

	@Schema(description = "유저 이메일", example = "test@gmail.com")
	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	@Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
	String email,

	@Schema(description = "인증번호", example = "1234")
	@NotBlank(message = "인증번호는 필수입니다.")
	@Pattern(regexp = "\\d{4}", message = "인증번호는 4자리 숫자입니다.")
	String code
) {
}
