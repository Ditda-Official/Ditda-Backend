package ditda.backend.domain.commission.draft.dto.response;

import java.util.List;

import ditda.backend.domain.commission.draft.entity.enums.WatermarkStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "1차 시안 목록 조회 응답")
public record DraftListResponse(

	@Schema(description = "외주 ID", example = "1")
	Long commissionId,

	@Schema(description = "외주명", example = "YBM 영어 교재 - 홍길동")
	String title,

	@Schema(description = "1차 시안 목록")
	List<DraftResponse> drafts
) {

	@Schema(description = "시안 정보")
	public record DraftResponse(

		@Schema(description = "시안 ID", example = "1")
		Long draftId,

		@Schema(description = "썸네일 URL (워터마크 미완료 시 null)", nullable = true)
		String thumbnailUrl,

		@Schema(description = "썸네일 워터마크 상태", example = "COMPLETED")
		WatermarkStatus watermarkStatus
	) {
	}
}
