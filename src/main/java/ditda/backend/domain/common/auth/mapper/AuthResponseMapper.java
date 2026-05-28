package ditda.backend.domain.common.auth.mapper;

import org.springframework.stereotype.Component;

import ditda.backend.domain.common.auth.dto.AuthResult;
import ditda.backend.domain.common.auth.dto.response.LoginResponse;
import ditda.backend.global.s3.S3UrlResolver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthResponseMapper {

	private final S3UrlResolver s3UrlResolver;

	public LoginResponse toLoginResponse(AuthResult result) {
		return new LoginResponse(
			result.userId(),
			result.name(),
			s3UrlResolver.toPublicS3Url(result.profileImage()),
			result.accessToken()
		);
	}
}
