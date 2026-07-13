package ditda.backend.domain.commission.revision.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "수정본 제출 응답")
public record RevisionSubmitResponse(

	@Schema(description = "생성된 시안 ID", example = "1")
	Long draftId,

	@Schema(description = "현재 수정 차수", example = "2")
	int currentRevisionCount,

	@Schema(description = "최대 수정 횟수", example = "3")
	int maxRevisionCount,

	@Schema(description = "제출 일시")
	LocalDateTime createdAt
) {
}
