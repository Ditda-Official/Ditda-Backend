package ditda.backend.domain.settlement.dto.response;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CategoryType;
import ditda.backend.domain.settlement.entity.Settlement;
import ditda.backend.domain.settlement.entity.enums.SettlementType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지급 내역 항목")
public record DesignerSettlementItemResponse(

	@Schema(description = "외주 ID", example = "12")
	Long commissionId,

	@Schema(description = "카테고리", example = "FLYER_TEXTBOOK_COVER_INNER")
	CategoryType category,

	@Schema(description = "제목", example = "YBM 영어교재 표지디자인 외주")
	String title,

	@Schema(description = "지급 종류", example = "BASE")
	SettlementType amountType,

	@Schema(description = "지급 금액", example = "40000")
	int amount
) {
	public static DesignerSettlementItemResponse from(Settlement settlement) {
		Commission commission = settlement.getCommission();
		return new DesignerSettlementItemResponse(
			commission.getId(),
			commission.getCategoryType(),
			commission.getTitle(),
			settlement.getSettlementType(),
			settlement.getAmount()
		);
	}
}
