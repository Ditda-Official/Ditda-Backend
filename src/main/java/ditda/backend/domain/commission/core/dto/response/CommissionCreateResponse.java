package ditda.backend.domain.commission.core.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CategoryType;
import ditda.backend.domain.commission.core.entity.enums.CommissionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "새 외주 작성 응답")
public record CommissionCreateResponse(

	@Schema(description = "외주 ID", example = "1")
	Long commissionId,

	@Schema(description = "제목", example = "수학의 정석 - 수학")
	String title,

	@Schema(description = "카테고리", example = "FLYER_TEXTBOOK_COVER_INNER")
	CategoryType category,

	@Schema(description = "상태", example = "PENDING")
	CommissionStatus status,

	@Schema(description = "지원 마감일", example = "2026-06-08")
	LocalDate applicationDeadline,

	@Schema(description = "1차 시안 마감일", example = "2026-06-15")
	LocalDate firstDraftDeadline,

	@Schema(description = "최종 마감일", example = "2026-06-23")
	LocalDate finalDeadline,

	@Schema(description = "최대 수정 횟수", example = "3")
	int maxRevision,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	LocalDateTime createdAt
) {

	public static CommissionCreateResponse from(Commission commission) {
		return new CommissionCreateResponse(
			commission.getId(),
			commission.getTitle(),
			commission.getCategoryType(),
			commission.getStatus(),
			commission.getApplicationDeadline(),
			commission.getFirstDraftDeadline(),
			commission.getFinalDeadline(),
			commission.getMaxRevision(),
			commission.getCreatedAt()
		);
	}
}