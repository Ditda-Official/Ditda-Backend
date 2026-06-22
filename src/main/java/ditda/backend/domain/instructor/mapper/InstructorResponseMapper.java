package ditda.backend.domain.instructor.mapper;

import org.springframework.stereotype.Component;

import ditda.backend.domain.instructor.dto.response.InstructorDetailResponse;
import ditda.backend.global.s3.manager.S3UrlResolver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InstructorResponseMapper {

	private final S3UrlResolver s3UrlResolver;

	public InstructorDetailResponse toInstructorDetailResponse(
		String name,
		String profileImageKey,
		long totalCount,
		long ongoingCount
	) {

		return new InstructorDetailResponse(
			name,
			s3UrlResolver.toPublicS3Url(profileImageKey),
			new InstructorDetailResponse.InstructorStats(totalCount, ongoingCount)
		);
	}
}
