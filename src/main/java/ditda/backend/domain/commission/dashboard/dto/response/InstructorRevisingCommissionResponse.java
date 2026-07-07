package ditda.backend.domain.commission.dashboard.dto.response;

import java.time.LocalDate;
import java.util.List;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.dashboard.repository.projection.InstructorRevisingView;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "수정 중인 외주 응답")
public record InstructorRevisingCommissionResponse(

	List<CommissionItem> commissions
) {
	public static InstructorRevisingCommissionResponse of(List<InstructorRevisingView> views) {

		List<CommissionItem> items = views.stream()
			.map(CommissionItem::from)
			.toList();

		return new InstructorRevisingCommissionResponse(items);
	}

	public record CommissionItem(
		@Schema(description = "외주 ID", example = "1")
		Long commissionId,

		@Schema(description = "제목", example = "수학의 정석 - 수학")
		String title,

		@Schema(description = "수정 요청 전송 여부 (true=전송완료, false=확인하기)", example = "true")
		boolean isSubmitted,

		@Schema(description = "수정사항 제출 여부", example = "true")
		boolean hasUpdated,

		@Schema(description = "최종 마감일", example = "2026-06-23")
		LocalDate finalDeadline
	) {

		private static CommissionItem from(InstructorRevisingView view) {

			Commission commission = view.getCommission();

			return new CommissionItem(
				commission.getId(),
				commission.getTitle(),
				view.getSubmitted(),
				view.getHasUpdated(),
				commission.getFinalDeadline()
			);
		}
	}
}


