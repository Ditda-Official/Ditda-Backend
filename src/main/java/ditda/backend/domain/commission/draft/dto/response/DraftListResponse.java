package ditda.backend.domain.commission.draft.dto.response;

import java.util.List;

import ditda.backend.domain.commission.core.entity.Commission;
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

	public static DraftListResponse of(Commission commission, List<DraftResponse> drafts) {
		return new DraftListResponse(commission.getId(), commission.getTitle(), drafts);
	}

	// 상세 조회에서 재사용할 수 있으면 빼놓고 import로 가져오는 방법 기억해두기.
	@Schema(description = "시안 정보")
	public record DraftResponse(

		@Schema(description = "시안 ID", example = "1")
		Long draftId,

		@Schema(description = "썸네일 URL")
		String thumbnailUrl
	) {
	}
}
