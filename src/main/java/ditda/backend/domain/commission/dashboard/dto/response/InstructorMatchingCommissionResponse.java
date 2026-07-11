package ditda.backend.domain.commission.dashboard.dto.response;

import java.time.LocalDate;
import java.util.List;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.dashboard.repository.projection.InstructorMatchingView;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "매칭 중인 외주 응답")
public record InstructorMatchingCommissionResponse(

	List<CommissionItem> commissions
) {
	public static InstructorMatchingCommissionResponse of(List<InstructorMatchingView> views) {

		List<CommissionItem> items = views.stream()
			.map(CommissionItem::from)
			.toList();

		return new InstructorMatchingCommissionResponse(items);
	}

	public record CommissionItem(
		@Schema(description = "외주 ID", example = "1")
		Long commissionId,

		@Schema(description = "제목", example = "수학의 정석 - 수학")
		String title,

		@Schema(description = "매칭 현황")
		MatchStatusResponse matching,

		@Schema(description = "디자이너 모집 마감일", example = "2026-06-23")
		LocalDate applicationDeadline
	) {
		private static CommissionItem from(InstructorMatchingView view) {

			Commission commission = view.getCommission();
			int matched = commission.matchedCount(
				view.getDistinctLevelCount().intValue(),
				view.getTotalCount().intValue()
			);

			return new CommissionItem(
				commission.getId(),
				commission.getTitle(),
				new MatchStatusResponse(matched, commission.getDesignerCount()),
				commission.getApplicationDeadline()
			);
		}

		@Schema(description = "매칭 현황")
		public record MatchStatusResponse(

			@Schema(description = "매칭된 디자이너 수", example = "2")
			int matched,

			@Schema(description = "총 모집 디자이너 수", example = "5")
			int total
		) {
		}
	}
}

