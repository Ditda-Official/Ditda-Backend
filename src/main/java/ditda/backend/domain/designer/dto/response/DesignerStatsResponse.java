package ditda.backend.domain.designer.dto.response;

import ditda.backend.domain.designer.entity.enums.DesignerLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "디자이너 마이페이지 통계 조회")
public record DesignerStatsResponse(

	@Schema(description = "디자이너 이름", example = "홍길동")
	String name,

	@Schema(description = "프로필 이미지 URL")
	String profileImageUrl,

	@Schema(description = "레벨/경험치 정보")
	LevelInfo levelInfo,

	@Schema(description = "디자이너 외주 통계")
	DesignerStats stats
) {

	@Schema(description = "레벨/경험치 정보")
	public record LevelInfo(

		@Schema(description = "레벨", example = "LEVEL_2")
		DesignerLevel level,

		@Schema(description = "현재 경험치", example = "563")
		int exp,

		@Schema(description = "다음 레벨로 넘어갈 수 있는 경험치", example = "1000")
		int requiredExp
	) {
	}

	@Schema(description = "디자이너 외주 통계")
	public record DesignerStats(

		@Schema(description = "누적 수입", example = "34343000")
		long income,

		@Schema(description = "외주 경험 횟수", example = "4")
		int submittedDraftCount,

		@Schema(description = "당첨률(%)", example = "80.0")
		double winRate
	) {
	}
}
