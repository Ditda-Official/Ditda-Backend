package ditda.backend.domain.common.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.common.auth.dto.request.EmailVerificationRequest;
import ditda.backend.domain.common.auth.facade.AuthFacade;
import ditda.backend.global.apipayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

	private final AuthFacade authFacade;

	@Operation(summary = "이메일 인증번호 요청", description = "이메일 인증번호를 요청합니다.")
	@PostMapping("/emails/verification-request")
	public ApiResponse<Void> requestEmailVerification(
		@Valid @RequestBody EmailVerificationRequest request
	) {
		authFacade.requestEmailVerification(request.email());

		return ApiResponse.onSuccess("인증번호가 발송되었습니다.");
	}
}
