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
		DesignerStatsResponse.DesignerStats stats
	) {

		return new DesignerStatsResponse(
			designer.getName(),
			s3UrlResolver.toPublicS3Url(designer.getProfileImage()),
			new DesignerStatsResponse.LevelInfo(
				designer.getLevel(),
				designer.getExp(),
				designer.getLevel().getRequiredExp()),
			stats
		);
	}
}
