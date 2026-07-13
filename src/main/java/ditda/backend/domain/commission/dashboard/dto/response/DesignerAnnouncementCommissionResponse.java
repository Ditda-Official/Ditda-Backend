package ditda.backend.domain.commission.dashboard.dto.response;

import java.time.LocalDate;
import java.util.List;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.dashboard.dto.response.enums.AnnouncementResult;
import ditda.backend.domain.commission.dashboard.repository.projection.DesignerAnnouncementView;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "발표 대기 외주 응답")
public record DesignerAnnouncementCommissionResponse(

	@Schema(description = "발표 대기 외주 목록")
	List<CommissionItem> commissions
) {

	public static DesignerAnnouncementCommissionResponse of(List<DesignerAnnouncementView> views) {

		List<CommissionItem> items = views.stream()
			.map(CommissionItem::from)
			.toList();

		return new DesignerAnnouncementCommissionResponse(items);
	}

	public record CommissionItem(

		@Schema(description = "외주 ID", example = "1")
		Long commissionId,

		@Schema(description = "제목", example = "수학의 정석 - 수학")
		String title,

		@Schema(description = "발표 결과", example = "AWAITING")
		AnnouncementResult status,

		@Schema(description = "지원 마감일", example = "2026-06-23")
		LocalDate applicationDeadline
	) {

		public static CommissionItem from(DesignerAnnouncementView view) {

			Commission commission = view.getCommission();

			return new CommissionItem(
				commission.getId(),
				commission.getTitle(),
				AnnouncementResult.from(view.getApplicationStatus()),
				commission.getApplicationDeadline()
			);
		}
	}
}

