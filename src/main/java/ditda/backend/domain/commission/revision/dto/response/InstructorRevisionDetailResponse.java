package ditda.backend.domain.commission.revision.dto.response;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "수정 내용 조회 응답")
public record InstructorRevisionDetailResponse(

	@Schema(description = "외주 ID", example = "1")
	Long commissionId,

	@Schema(description = "제목", example = "수학의 정석 - 수학")
	String title,

	@Schema(description = "시안 정보")
	DraftInfo draft,

	@Schema(description = "현재 수정 차수 (0부터 시작)", example = "2")
	Integer currentRevisionCount,

	@Schema(description = "최대 수정 횟수", example = "3")
	Integer maxRevisionCount

) {

	public static InstructorRevisionDetailResponse of(
		Commission commission,
		CommissionDraft draft,
		String thumbnailUrl,
		String designerComment,
		int currentRevisionCount
	) {
		return new InstructorRevisionDetailResponse(
			commission.getId(),
			commission.getTitle(),
			new DraftInfo(draft.getId(), thumbnailUrl, designerComment),
			currentRevisionCount,
			commission.getMaxRevision()
		);
	}

	public record DraftInfo(

		@Schema(description = "시안 ID", example = "1")
		Long draftId,

		@Schema(description = "시안 썸네일", example = "https://example.com/commission/draft/thumbnail.png", nullable = true)
		String thumbnailUrl,

		@Schema(description = "디자이너 코멘트", example = "지난번 수정사항 모두 반영했습니다.", nullable = true)
		String designerComment
	) {

	}
}
