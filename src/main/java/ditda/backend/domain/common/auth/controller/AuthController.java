package ditda.backend.domain.common.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.common.auth.dto.AuthResult;
import ditda.backend.domain.common.auth.dto.request.EmailCodeVerificationRequest;
import ditda.backend.domain.common.auth.dto.request.EmailVerificationRequest;
import ditda.backend.domain.common.auth.dto.request.LoginRequest;
import ditda.backend.domain.common.auth.dto.response.LoginResponse;
import ditda.backend.domain.common.auth.dto.response.ReissueResponse;
import ditda.backend.domain.common.auth.facade.AuthFacade;
import ditda.backend.global.apipayload.response.ApiResponse;
import ditda.backend.global.jwt.utils.CookieUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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
	@PostMapping("/emails/verification-requests")
	public ApiResponse<Void> requestEmailVerification(
		@Valid @RequestBody EmailVerificationRequest request
	) {
		authFacade.requestEmailVerification(request.email());

		return ApiResponse.onSuccess("인증번호가 발송되었습니다.");
	}

	@Operation(summary = "이메일 인증번호 검증", description = "이메일 인증번호를 검증합니다.")
	@PostMapping("/emails/verifications")
	public ApiResponse<Void> verifyEmailCode(
		@Valid @RequestBody EmailCodeVerificationRequest request
	) {
		authFacade.verifyEmailCode(request.email(), request.code());
		return ApiResponse.onSuccess("이메일 인증이 완료되었습니다.");
	}

	@Operation(summary = "로그인", description = "**[로그인]** 아이디/비밀번호로 로그인합니다.")
	@PostMapping("/login")
	public ApiResponse<LoginResponse> login(
		@Valid @RequestBody LoginRequest request,
		HttpServletResponse response
	) {

		AuthResult result = authFacade.login(request);
		response.addHeader(HttpHeaders.SET_COOKIE, result.refreshTokenCookie().toString());

		return ApiResponse.onSuccess("로그인 성공",
			new LoginResponse(result.userId(), result.accessToken()));
	}

	@Operation(summary = "로그아웃", description = "**[공통]** 인증된 사용자의 토큰을 무효화합니다.")
	@PostMapping("/logout")
	public ApiResponse<Void> logout(
		@AuthenticationPrincipal Long userId,
		@CookieValue(value = CookieUtils.REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
		HttpServletResponse response
	) {

		ResponseCookie deleted = authFacade.logout(userId, refreshToken);
		response.addHeader(HttpHeaders.SET_COOKIE, deleted.toString());

		return ApiResponse.onSuccess("로그아웃 성공");
	}

	@Operation(summary = "토큰 재발급", description = "**[공통]** refresh 토큰으로 새 access 토큰을 발급받습니다.")
	@PostMapping("/reissue")
	public ApiResponse<ReissueResponse> refresh(
		@Parameter(hidden = true)
		@CookieValue(value = CookieUtils.REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
		HttpServletResponse response
	) {
		AuthResult result = authFacade.reissue(refreshToken);
		response.addHeader(HttpHeaders.SET_COOKIE, result.refreshTokenCookie().toString());

		return ApiResponse.onSuccess("토큰 재발급 성공",
			new ReissueResponse(result.accessToken()));
	}
}
