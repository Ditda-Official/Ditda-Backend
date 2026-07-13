package ditda.backend.domain.admin.designer.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import ditda.backend.domain.admin.designer.dto.response.DesignerPortfolioResponse;
import ditda.backend.global.s3.manager.S3PresignedUrlGenerator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminDesignerMapper {

	private final S3PresignedUrlGenerator s3PresignedUrlGenerator;

	public DesignerPortfolioResponse toPortfolioResponse(Long designerId, List<String> portfolioKeys) {

		List<String> portfolioUrls = portfolioKeys.stream()
			.map(s3PresignedUrlGenerator::generatePrivateGetUrl)
			.toList();

		return DesignerPortfolioResponse.of(designerId, portfolioUrls);
	}
}
