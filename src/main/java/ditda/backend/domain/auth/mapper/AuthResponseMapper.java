package ditda.backend.domain.auth.mapper;

import org.springframework.stereotype.Component;

import ditda.backend.domain.auth.dto.AuthResult;
import ditda.backend.domain.auth.dto.response.LoginResponse;
import ditda.backend.domain.auth.dto.response.SignupResponse;
import ditda.backend.global.s3.manager.S3UrlResolver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthResponseMapper {

	private final S3UrlResolver s3UrlResolver;

	public LoginResponse toLoginResponse(AuthResult result) {
		return new LoginResponse(
			result.userId(),
			result.userRole(),
			result.name(),
			s3UrlResolver.toPublicS3Url(result.profileImage()),
			result.accessToken()
		);
	}

	public SignupResponse toSignupResponse(AuthResult result) {
		return new SignupResponse(
			result.userId(),
			result.userRole(),
			result.name(),
			s3UrlResolver.toPublicS3Url(result.profileImage()),
			result.accessToken()
		);
	}
}
