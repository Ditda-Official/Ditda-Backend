package ditda.backend.domain.admin.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.admin.auth.dto.request.AdminLoginRequest;
import ditda.backend.domain.admin.auth.dto.response.AdminLoginResponse;
import ditda.backend.domain.admin.auth.service.AdminAuthService;
import ditda.backend.global.apipayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
@Tag(name = "Admin Auth", description = "어드민 인증 API")
public class AdminAuthController {

	private final AdminAuthService adminAuthService;

	@Operation(summary = "어드민 로그인", description = "**[어드민]** 어드민 계정으로 로그인합니다.")
	@PostMapping("/login")
	public ApiResponse<AdminLoginResponse> login(
		@Valid @RequestBody AdminLoginRequest request
	) {

		AdminLoginResponse response = adminAuthService.login(request);

		return ApiResponse.onSuccess("어드민 로그인 성공", response);
	}
}
