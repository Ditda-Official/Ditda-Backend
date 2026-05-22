package ditda.backend.domain.instructor.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.instructor.auth.dto.InstructorAuthResult;
import ditda.backend.domain.instructor.auth.dto.request.CheckUsernameRequest;
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
@RequestMapping("/api/v1/instructors")
@RequiredArgsConstructor
@Tag(name = "Instructor Auth", description = "강사 회원가입 API")
public class InstructorAuthController {

	private final InstructorAuthFacade instructorAuthFacade;

	@Operation(summary = "강사 아이디 중복 확인", description = "**[회원가입]** 사용 가능한 아이디인지 확인합니다.")
	@GetMapping("/check-username")
	public ApiResponse<Void> checkUsername(
		@Valid @RequestBody CheckUsernameRequest request
	) {

		instructorAuthFacade.validateUsernameAvailable(request.username());
		return ApiResponse.onSuccess("아이디 사용 가능 여부 조회 성공");
	}

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
