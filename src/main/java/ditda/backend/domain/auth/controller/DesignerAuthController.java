package ditda.backend.domain.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.auth.dto.AuthResult;
import ditda.backend.domain.auth.dto.request.DesignerSignupRequest;
import ditda.backend.domain.auth.dto.request.PortfolioPresignRequest;
import ditda.backend.domain.auth.dto.response.PortfolioPresignResponse;
import ditda.backend.domain.auth.dto.response.SignupResponse;
import ditda.backend.domain.auth.facade.DesignerAuthFacade;
import ditda.backend.domain.auth.mapper.AuthResponseMapper;
import ditda.backend.global.apipayload.response.ApiResponse;
import ditda.backend.global.jwt.utils.CookieUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/designers/auth")
@RequiredArgsConstructor
@Tag(name = "Designer Auth", description = "디자이너 회원가입 API")
public class DesignerAuthController {

	private final DesignerAuthFacade designerAuthFacade;
	private final CookieUtils cookieUtils;
	private final AuthResponseMapper authResponseMapper;

	@Operation(
		summary = "포트폴리오 업로드 URL 발급",
		description = "**[회원가입]** 이메일 인증 완료 후, 포트폴리오 파일을 S3에 올릴 presigned PUT URL을 발급합니다. "
			+ "PUT URL로 S3에 직접 업로드한 뒤, 응답의 key를 회원가입 요청의 portfolioKeys에 담아 전송합니다."
	)
	@PostMapping("/signup/portfolio/presigned-url")
	public ApiResponse<PortfolioPresignResponse> issuePortfolioPresignedUrls(
		@Valid @RequestBody PortfolioPresignRequest request
	) {
		PortfolioPresignResponse response = designerAuthFacade.issuePortfolioPresignedUrl(request);

		return ApiResponse.onSuccess("포트폴리오 업로드 URL 발급 성공", response);
	}

	@Operation(
		summary = "디자이너 회원가입",
		description = "**[회원가입]** 회원가입 후 자동 로그인 처리됩니다. presigned PUT URL로 업로드한 포트폴리오 key를 portfolioKeys에 담아 전송합니다."
	)
	@PostMapping(value = "/signup")
	public ApiResponse<SignupResponse> signup(
		@Valid @RequestBody DesignerSignupRequest request,
		HttpServletResponse response
	) {
		AuthResult result = designerAuthFacade.signup(request);

		addRefreshTokenCookie(response, result.refreshToken());

		return ApiResponse.onSuccess("디자이너 회원가입 성공", authResponseMapper.toSignupResponse(result));
	}

	private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
		response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils.createRefreshTokenCookie(refreshToken).toString());
	}
}
