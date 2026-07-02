package ditda.backend.domain.settlement.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CategoryType;
import ditda.backend.domain.settlement.entity.Settlement;
import ditda.backend.domain.settlement.entity.enums.SettlementType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "디자이너 지급 내역 응답")
public record DesignerSettlementResponse(

	@Schema(description = "지급 내역 목록")
	List<SettlementItem> items,

	@Schema(description = "현재 페이지 번호", example = "0")
	int page,

	@Schema(description = "페이지 크기", example = "10")
	int size,

	@Schema(description = "전체 데이터 수", example = "23")
	long totalElements,

	@Schema(description = "전체 페이지 수", example = "3")
	int totalPages
) {
	public static DesignerSettlementResponse from(Page<Settlement> page) {

		List<SettlementItem> items = page.getContent().stream()
			.map(SettlementItem::from)
			.toList();

		return new DesignerSettlementResponse(
			items,
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages()
		);
	}

	@Schema(description = "지급 내역 항목")
	public record SettlementItem(

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
		private static SettlementItem from(Settlement settlement) {
			Commission commission = settlement.getCommission();
			return new SettlementItem(
				commission.getId(),
				commission.getCategoryType(),
				commission.getTitle(),
				settlement.getSettlementType(),
				settlement.getAmount()
			);
		}
	}
}
