package ditda.backend.domain.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.auth.dto.AuthResult;
import ditda.backend.domain.auth.dto.request.InstructorSignupRequest;
import ditda.backend.domain.auth.dto.response.SignupResponse;
import ditda.backend.domain.auth.facade.InstructorAuthFacade;
import ditda.backend.domain.auth.mapper.AuthResponseMapper;
import ditda.backend.global.apipayload.response.ApiResponse;
import ditda.backend.global.jwt.utils.CookieUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/instructors/auth")
@RequiredArgsConstructor
@Tag(name = "Instructor Auth", description = "강사 회원가입 API")
public class InstructorAuthController {

	private final InstructorAuthFacade instructorAuthFacade;
	private final CookieUtils cookieUtils;
	private final AuthResponseMapper authResponseMapper;

	@Operation(summary = "강사 회원가입", description = "**[회원가입]** 회원가입 후 자동 로그인 처리됩니다.")
	@PostMapping("/signup")
	public ApiResponse<SignupResponse> signup(
		@Valid @RequestBody InstructorSignupRequest request,
		HttpServletResponse response
	) {

		AuthResult result = instructorAuthFacade.signup(request);

		addRefreshTokenCookie(response, result.refreshToken());

		return ApiResponse.onSuccess("강사 회원가입 성공", authResponseMapper.toSignupResponse(result));
	}

	private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
		response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils.createRefreshTokenCookie(refreshToken).toString());
	}
}
