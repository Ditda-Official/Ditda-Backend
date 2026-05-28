package ditda.backend.domain.designer.auth.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ditda.backend.domain.designer.auth.dto.DesignerAuthResult;
import ditda.backend.domain.designer.auth.dto.request.DesignerSignupRequest;
import ditda.backend.domain.designer.auth.dto.response.DesignerSignupResponse;
import ditda.backend.domain.designer.auth.facade.DesignerAuthFacade;
import ditda.backend.global.apipayload.response.ApiResponse;
import ditda.backend.global.jwt.utils.CookieUtils;
import ditda.backend.global.s3.S3UrlResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
	private final S3UrlResolver s3UrlResolver;
	private final CookieUtils cookieUtils;

	@Operation(
		summary = "디자이너 회원가입",
		description = "**[회원가입]** 회원가입 후 자동 로그인 처리됩니다. `data`(JSON) + `portfolioFiles`(파일 최대 3개, 각 30MB)"
	)
	@RequestBody(
		content = @Content(
			mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
			encoding = @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE)
		)
	)
	@PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<DesignerSignupResponse> signup(
		@Valid @RequestPart("data") DesignerSignupRequest request,
		@RequestPart(value = "portfolioFiles", required = false) List<MultipartFile> portfolioFiles,
		HttpServletResponse response
	) {

		DesignerAuthResult result = designerAuthFacade.signup(request, portfolioFiles);

		addRefreshTokenCookie(response, result.refreshToken());

		DesignerSignupResponse signupResponse = new DesignerSignupResponse(
			result.userId(),
			result.name(),
			s3UrlResolver.toPublicS3Url(result.profileImage()),
			result.accessToken());

		return ApiResponse.onSuccess("디자이너 회원가입 성공", signupResponse);
	}

	private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
		response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils.createRefreshTokenCookie(refreshToken).toString());
	}
}
