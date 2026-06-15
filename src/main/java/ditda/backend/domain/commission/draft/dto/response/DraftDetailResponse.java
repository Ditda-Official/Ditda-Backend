package ditda.backend.domain.commission.draft.dto.response;

import java.util.List;

import ditda.backend.domain.commission.draft.entity.enums.WatermarkStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시안 상세 조회 응답")
public record DraftDetailResponse(

	@Schema(description = "외주 ID", example = "1")
	Long commissionId,

	@Schema(description = "시안 ID", example = "1")
	Long draftId,

	@Schema(description = "시안 파일 목록")
	List<FileResponse> files
) {

	@Schema(description = "시안 파일 정보")
	public record FileResponse(

		@Schema(description = "페이지 순서", example = "0")
		int fileOrder,

		@Schema(description = "파일 URL (워터마크 미완료 시 null)", nullable = true)
		String url,

		@Schema(description = "워터마크 상태", example = "COMPLETED")
		WatermarkStatus watermarkStatus
	) {
	}
}
