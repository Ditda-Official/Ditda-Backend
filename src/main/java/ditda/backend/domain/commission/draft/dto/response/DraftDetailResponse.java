package ditda.backend.domain.commission.draft.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시안 상세 조회 응답")
public record DraftDetailResponse(

	@Schema(description = "외주 ID", example = "1")
	Long commissionId,

	@Schema(description = "시안 ID", example = "1")
	Long draftId,

	@Schema(description = "시안 파일 URL 목록")
	List<String> fileUrls
) {

	public static DraftDetailResponse of(Long commissionId, Long draftId, List<String> fileUrls) {
		return new DraftDetailResponse(commissionId, draftId, fileUrls);
	}
}
