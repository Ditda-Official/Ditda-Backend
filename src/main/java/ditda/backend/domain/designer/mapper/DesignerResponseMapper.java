package ditda.backend.domain.designer.mapper;

import org.springframework.stereotype.Component;

import ditda.backend.domain.designer.dto.response.DesignerStatsResponse;
import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.global.s3.manager.S3UrlResolver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DesignerResponseMapper {

	private final S3UrlResolver s3UrlResolver;

	public DesignerStatsResponse toDesignerStatsResponse(
		Designer designer,
		long income,
		int submittedDraftCount,
		int selectedDraftCount
	) {

		double winRate = submittedDraftCount == 0 ? 0.0
			: (double)selectedDraftCount / submittedDraftCount * 100;

		return new DesignerStatsResponse(
			designer.getUser().getName(),
			s3UrlResolver.toPublicS3Url(designer.getUser().getProfileImage()),
			new DesignerStatsResponse.LevelInfo(
				designer.getLevel(),
				designer.getExp(),
				designer.getLevel().getRequiredExp()),
			new DesignerStatsResponse.DesignerStats(
				income,
				submittedDraftCount,
				winRate
			)
		);
	}

}
