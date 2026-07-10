package ditda.backend.domain.commission.dashboard.dto.response;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CategoryType;
import ditda.backend.domain.commission.dashboard.repository.projection.DesignerDraftSubmissionView;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시안 제출 예정 외주 응답")
public record DesignerDraftSubmissionCommissionResponse(

	@Schema(description = "시안 제출 예정 외주 목록")
	List<CommissionItem> commissions
) {

	public static DesignerDraftSubmissionCommissionResponse of(
		List<DesignerDraftSubmissionView> views,
		Map<Long, Integer> maxAmountMap
	) {
		List<CommissionItem> items = views.stream()
			.map(view -> CommissionItem.from(view, maxAmountMap.get(view.getCommission().getId())))
			.toList();

		return new DesignerDraftSubmissionCommissionResponse(items);
	}

	@Schema(description = "시안 제출 예정 외주 정보")
	public record CommissionItem(

		@Schema(description = "외주 ID", example = "1")
		Long commissionId,

		@Schema(description = "제목", example = "수학의 정석 - 수학")
		String title,

		@Schema(description = "카테고리", example = "FLYER_TEXTBOOK_COVER_INNER")
		CategoryType category,

		@Schema(description = "시안 제출 여부", example = "true")
		boolean isSubmitted,

		@Schema(description = "제출 마감일", example = "2026-06-23T23:59:00")
		LocalDateTime submitDeadline,

		@Schema(description = "최대 수령액", example = "210000")
		int maxAmount
	) {

		private static final LocalTime SUBMIT_DEADLINE_TIME = LocalTime.of(23, 59);

		public static CommissionItem from(DesignerDraftSubmissionView view, int maxAmount) {

			Commission commission = view.getCommission();

			return new CommissionItem(
				commission.getId(),
				commission.getTitle(),
				commission.getCategoryType(),
				view.getApplicationStatus() == ApplicationStatus.DRAFT_SUBMITTED,
				commission.getFirstDraftDeadline().atTime(SUBMIT_DEADLINE_TIME),
				maxAmount
			);
		}
	}
}
