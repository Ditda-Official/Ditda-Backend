package ditda.backend.domain.commission.history.dto.response;

import java.time.LocalDate;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CategoryType;
import ditda.backend.domain.commission.core.entity.enums.CommissionStatus;
import ditda.backend.domain.commission.core.entity.enums.PlanCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강사 외주 내역 항목")
public record InstructorCommissionItemResponse(

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
	public static InstructorCommissionItemResponse from(Commission commission, Integer paidAmount) {
		return new InstructorCommissionItemResponse(
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
