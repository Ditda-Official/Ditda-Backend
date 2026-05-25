package ditda.backend.domain.instructor.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.instructor.auth.dto.InstructorAuthResult;
import ditda.backend.domain.instructor.auth.dto.request.InstructorSignupRequest;
import ditda.backend.domain.instructor.auth.dto.response.InstructorSignupResponse;
import ditda.backend.domain.instructor.auth.facade.InstructorAuthFacade;
import ditda.backend.global.apipayload.response.ApiResponse;
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

	@Operation(summary = "강사 회원가입", description = "**[회원가입]** 회원가입 후 자동 로그인 처리됩니다.")
	@PostMapping("/signup")
	public ApiResponse<InstructorSignupResponse> signup(
		@Valid @RequestBody InstructorSignupRequest request,
		HttpServletResponse response
	) {

		InstructorAuthResult result = instructorAuthFacade.signup(request);

		response.addHeader(HttpHeaders.SET_COOKIE, result.refreshTokenCookie().toString());

		return ApiResponse.onSuccess("강사 회원가입 성공",
			new InstructorSignupResponse(result.userId(), result.accessToken()));
	}
}
