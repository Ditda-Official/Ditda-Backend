package ditda.backend.domain.instructor.auth.mapper;

import org.springframework.stereotype.Component;

import ditda.backend.domain.instructor.auth.dto.InstructorAuthResult;
import ditda.backend.domain.instructor.auth.dto.response.InstructorSignupResponse;
import ditda.backend.global.s3.S3UrlResolver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InstructorAuthResponseMapper {

	private final S3UrlResolver s3UrlResolver;

	public InstructorSignupResponse toSignupResponse(InstructorAuthResult result) {
		return new InstructorSignupResponse(
			result.userId(),
			result.name(),
			s3UrlResolver.toPublicS3Url(result.profileImage()),
			result.accessToken()
		);
	}
}
