package ditda.backend.domain.commission.history.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CategoryType;
import ditda.backend.domain.commission.core.entity.enums.CommissionStatus;
import ditda.backend.domain.commission.core.entity.enums.PlanCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강사 외주 내역 응답")
public record InstructorCommissionHistoryResponse(

	@Schema(description = "외주 목록")
	List<CommissionItem> items,

	@Schema(description = "현재 페이지 번호", example = "0")
	int page,

	@Schema(description = "페이지 크기", example = "10")
	int size,

	@Schema(description = "전체 데이터 수", example = "23")
	long totalElements,

	@Schema(description = "전체 페이지 수", example = "3")
	int totalPages
) {
	public static InstructorCommissionHistoryResponse from(
		Page<Commission> page,
		Map<Long, Integer> paidAmounts
	) {

		List<CommissionItem> items = page.getContent().stream()
			.map(commission -> CommissionItem.from(
				commission,
				paidAmounts.get(commission.getId())))
			.toList();

		return new InstructorCommissionHistoryResponse(
			items,
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages()
		);
	}

	@Schema(description = "외주 내역 항목")
	public record CommissionItem(

		@Schema(description = "외주 ID", example = "1")
		Long commissionId,

		@Schema(description = "카테고리", example = "FLYER_TEXTBOOK_COVER_INNER")
		CategoryType category,

		@Schema(description = "제목", example = "YBM 영어교재 표지디자인 외주")
		String title,

		@Schema(description = "생성일", example = "2025-05-05")
		LocalDate createdAt,

		@Schema(description = "플랜", example = "BASIC")
		PlanCode plan,

		@Schema(description = "결제 금액", example = "400000")
		Integer paidAmount,

		@Schema(description = "상태", example = "COMPLETED")
		CommissionStatus status
	) {
		private static CommissionItem from(Commission commission, Integer paidAmount) {
			return new CommissionItem(
				commission.getId(),
				commission.getCategoryType(),
				commission.getTitle(),
				commission.getCreatedAt().toLocalDate(),
				commission.getPlanCode(),
				paidAmount,
				commission.getStatus()
			);
		}
	}
}
