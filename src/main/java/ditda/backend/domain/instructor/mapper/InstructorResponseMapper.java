package ditda.backend.domain.instructor.mapper;

import org.springframework.stereotype.Component;

import ditda.backend.domain.instructor.dto.response.InstructorStatsResponse;
import ditda.backend.global.s3.manager.S3UrlResolver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InstructorResponseMapper {

	private final S3UrlResolver s3UrlResolver;

	public InstructorStatsResponse toInstructorStatsResponse(
		String name,
		String profileImageKey,
		long totalCount,
		long ongoingCount
	) {

		return new InstructorStatsResponse(
			name,
			s3UrlResolver.toPublicS3Url(profileImageKey),
			new InstructorStatsResponse.InstructorStats(totalCount, ongoingCount)
		);
	}
}
