package ditda.backend.domain.designer.auth.mapper;

import org.springframework.stereotype.Component;

import ditda.backend.domain.designer.auth.dto.DesignerAuthResult;
import ditda.backend.domain.designer.auth.dto.response.DesignerSignupResponse;
import ditda.backend.global.s3.S3UrlResolver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DesignerAuthResponseMapper {

	private final S3UrlResolver s3UrlResolver;

	public DesignerSignupResponse toSignupResponse(DesignerAuthResult result) {
		return new DesignerSignupResponse(
			result.userId(),
			result.name(),
			s3UrlResolver.toPublicS3Url(result.profileImage()),
			result.accessToken()
		);
	}
}
