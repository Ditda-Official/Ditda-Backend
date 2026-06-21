package ditda.backend.domain.auth.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import ditda.backend.domain.term.entity.enums.TermType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "강사 회원가입 요청")
public record InstructorSignupRequest(

	@Schema(
		description = "약관",
		example = """
			[
				{
					"type": "SERVICE",
					"version": "V1.0",
					"isAgreed": true
				},
				{
					"type": "USERINFO",
					"version": "V1.0",
					"isAgreed": true
				},
				{
					"type": "SETTLEMENT",
					"version": "V1.0",
					"isAgreed": true
				},
				{
					"type": "DISINTERMEDIATION",
					"version": "V1.0",
					"isAgreed": true
				}
			]
			"""
	)
	@NotEmpty(message = "약관 동의 여부 내용은 필수입니다")
	@Valid
	List<@NotNull(message = "약관 항목은 필수입니다.") @Valid TermRequest> terms,

	@Schema(description = "이름", example = "홍길동")
	@NotBlank(message = "이름은 필수입니다.")
	@Size(max = 50, message = "올바르지 않은 이름입니다.")
	String name,

	@Schema(description = "전화번호", example = "01012345678")
	@NotBlank(message = "전화번호가 없습니다.")
	@Pattern(
		regexp = "^010\\d{7,8}$",
		message = "전화번호 형식이 올바르지 않습니다."
	)
	String phone,

	@Schema(description = "아이디 (6 ~ 20자 이내)", example = "testid123")
	@NotBlank(message = "아이디는 필수입니다.")
	@Size(min = 6, max = 20, message = "아이디는 6자 이상 20자 이하여야 합니다.")
	String username,

	@Schema(description = "비밀번호", example = "password1234")
	@NotBlank(message = "비밀번호는 필수입니다.")
	@Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
		message = "비밀번호는 영문, 숫자 1개 이상 포함해야 합니다."
	)
	String password,

	@Schema(description = "이메일", example = "testid@gmail.com")
	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	@Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
	String email
) {

	public record TermRequest(

		@Schema(description = "약관 종류", example = "SERVICE")
		@NotNull(message = "약관 종류 필수입니다.")
		TermType type,

		@Schema(description = "약관 버전", example = "V1.0")
		@NotBlank(message = "약관 버전 필수입니다.")
		String version,

		@Schema(description = "약관 동의 여부", example = "true")
		@NotNull(message = "약관 동의 여부는 필수입니다.")
		@JsonProperty("isAgreed")
		Boolean isAgreed
	) {
	}
}
