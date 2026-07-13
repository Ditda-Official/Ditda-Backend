package ditda.backend.domain.commission.draft.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "1차 시안 제출 응답")
public record DraftSubmitResponse(

	@Schema(description = "외주 ID", example = "1")
	Long commissionId,

	@Schema(description = "생성된 시안 ID", example = "1")
	Long draftId,

	@Schema(description = "제출 시각", example = "2026-05-15 12:30:00")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime submittedAt
) {

}
