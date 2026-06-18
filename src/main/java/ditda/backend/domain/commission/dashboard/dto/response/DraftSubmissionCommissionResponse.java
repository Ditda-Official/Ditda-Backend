package ditda.backend.domain.commission.dashboard.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시안 제출 외주 응답")
public record DraftSubmissionCommissionResponse(
	List<CommissionItem> commissions
) {
	public static DraftSubmissionCommissionResponse of(
		List<Commission> commissions,
		Map<Long, Long> draftSubmissionCount,
		Map<Long, Boolean> viewable
	) {
		List<CommissionItem> items = commissions.stream()
			.map(c -> CommissionItem.from(
				c,
				draftSubmissionCount.get(c.getId()),
				viewable.get(c.getId())
			))
			.toList();

		return new DraftSubmissionCommissionResponse(items);
	}

	public record CommissionItem(
		@Schema(description = "외주 ID", example = "1")
		Long commissionId,

		@Schema(description = "제목", example = "수학의 정석 - 수학")
		String title,

		@Schema(description = "카테고리", example = "FLYER_TEXTBOOK_COVER_INNER")
		CategoryType category,

		@Schema(description = "1차 시안 제출 현황")
		DraftSubmissionStatus draftSubmission,

		@Schema(description = "시안 확인 가능 여부", example = "true")
		boolean isViewable,

		@Schema(description = "1차 시안 마감일", example = "2026-06-23")
		LocalDate firstDraftDeadline
	) {
		private static CommissionItem from(Commission commission, long submitted, boolean isViewable) {

			return new CommissionItem(
				commission.getId(),
				commission.getTitle(),
				commission.getCategoryType(),
				new DraftSubmissionStatus((int)submitted, commission.getDesignerCount()),
				isViewable,
				commission.getFirstDraftDeadline()
			);
		}

		@Schema(description = "1차 시안 제출 현황")
		public record DraftSubmissionStatus(

			@Schema(description = "제출한 디자이너 수", example = "2")
			int submitted,

			@Schema(description = "총 모집 디자이너 수", example = "5")
			int total
		) {
		}
	}
}
