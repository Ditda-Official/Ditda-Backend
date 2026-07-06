package ditda.backend.domain.commission.core.dto.response;

import java.time.LocalDate;

import ditda.backend.domain.commission.core.dto.PriceDetail;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모집 중 외주 목록 항목")
public record DesignerCommissionItemResponse(

	@Schema(description = "외주 ID", example = "1")
	Long commissionId,

	@Schema(description = "지원 마감일", example = "2026-05-05")
	LocalDate applicationDeadline,

	@Schema(description = "외주 카테고리", example = "FLYER_TEXTBOOK_COVER_INNER")
	CategoryType category,

	@Schema(description = "제목", example = "해커스톡 왕초보 - 홍길동")
	String title,

	@Schema(description = "기본금 (원)", example = "40000")
	int baseAmount,

	@Schema(description = "최대금액 (원)", example = "210000")
	int maxAmount
) {

	public static DesignerCommissionItemResponse from(Commission commission, PriceDetail priceDetail) {
		return new DesignerCommissionItemResponse(
			commission.getId(),
			commission.getApplicationDeadline(),
			commission.getCategoryType(),
			commission.getTitle(),
			priceDetail.baseAmount(),
			priceDetail.maxAmount()
		);
	}
}
