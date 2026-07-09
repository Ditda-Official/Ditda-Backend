package ditda.backend.domain.admin.designer.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "디자이너 포트폴리오 정보 응답")
public record DesignerPortfolioResponse(

	@Schema(description = "디자이너 ID", example = "1")
	Long designerId,

	@Schema(description = "포트폴리오 URL", example = "[\"https://example.com/portfolio/file.pdf\"]")
	List<String> portfolioUrls

) {

	public static DesignerPortfolioResponse of(Long designerId, List<String> portfolioUrls) {
		return new DesignerPortfolioResponse(designerId, portfolioUrls);
	}
}
