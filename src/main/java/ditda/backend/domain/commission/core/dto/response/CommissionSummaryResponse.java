package ditda.backend.domain.commission.core.dto.response;

import java.time.LocalDate;

import ditda.backend.domain.commission.core.entity.Commission;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "디자이너 시안 제출 페이지용 외주 정보")
public record CommissionSummaryResponse(

	@Schema(description = "외주 ID", example = "1")
	Long commissionId,

	@Schema(description = "제목", example = "수학의 정석 - 수학")
	String title,

	@Schema(description = "1차 시안 마감일", example = "2026-05-09")
	LocalDate firstDraftDeadline,

	@Schema(description = "최종 마감일", example = "2026-05-30")
	LocalDate finalDeadline

) {

	public static CommissionSummaryResponse from(Commission commission) {
		return new CommissionSummaryResponse(
			commission.getId(),
			commission.getTitle(),
			commission.getFirstDraftDeadline(),
			commission.getFinalDeadline()
		);
	}
}
