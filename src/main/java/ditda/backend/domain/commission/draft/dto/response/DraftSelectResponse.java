package ditda.backend.domain.commission.draft.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시안 선택 응답")
public record DraftSelectResponse(

	@Schema(description = "외주 ID", example = "1")
	Long commissionId,

	@Schema(description = "선택한 시안 ID", example = "1")
	Long selectedDraftId,

	@Schema(description = "선택된 시안 상태", example = "SELECTED")
	ApplicationStatus status,

	@Schema(description = "남은 수정 횟수", example = "3")
	int remainingRevisionCount,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	LocalDateTime selectedAt

) {
}
