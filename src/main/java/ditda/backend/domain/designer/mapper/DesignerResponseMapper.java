package ditda.backend.domain.designer.mapper;

import org.springframework.stereotype.Component;

import ditda.backend.domain.designer.dto.response.DesignerStatsResponse;
import ditda.backend.domain.designer.entity.enums.DesignerLevel;
import ditda.backend.global.s3.manager.S3UrlResolver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DesignerResponseMapper {

	private final S3UrlResolver s3UrlResolver;

	public DesignerStatsResponse toDesignerStatsResponse(
		String name,
		String profileImageKey,
		DesignerLevel level,
		int exp,
		int requiredExp,
		long income,
		int submittedDraftCount,
		double winRate
	) {

		return new DesignerStatsResponse(
			name,
			s3UrlResolver.toPublicS3Url(profileImageKey),
			new DesignerStatsResponse.LevelInfo(level, exp, requiredExp),
			new DesignerStatsResponse.DesignerStats(income, submittedDraftCount, winRate)
		);
	}
}
