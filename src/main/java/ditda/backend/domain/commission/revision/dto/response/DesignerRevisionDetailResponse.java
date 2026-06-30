package ditda.backend.domain.commission.revision.dto.response;

import java.time.LocalDate;
import java.util.List;

import ditda.backend.domain.commission.revision.entity.enums.RevisionCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "수정 요청 사항 조회 응답")
public record DesignerRevisionDetailResponse(

	@Schema(description = "수정 요청 ID", example = "1")
	Long revisionRequestId,

	@Schema(description = "외주 ID", example = "1")
	Long commissionId,

	@Schema(description = "제목", example = "수학의 정석 - 수학")
	String title,

	@Schema(description = "수정 마감일", example = "2026-05-09")
	LocalDate revisionDeadline,

	@Schema(description = "남은 수정 횟수", example = "2")
	Integer remainingRevisionCount,

	@Schema(description = "수정 대상 시안")
	TargetDraft targetDraft,

	@Schema(description = "수정 요청 항목 목록")
	List<RevisionDetail> revisionDetails
) {

	public record TargetDraft(

		@Schema(description = "시안 ID", example = "1")
		Long draftId,

		@Schema(description = "시안 썸네일", example = "https://example.com/commission/draft/thumbnail.png")
		String thumbnailUrl
	) {
	}

	public record RevisionDetail(

		@Schema(description = "수정 요청 카테고리", example = "LAYOUT")
		RevisionCategory category,

		@Schema(description = "수정 요청 내용", example = "레이아웃 수정해주세요.")
		String comment
	) {
	}

}
