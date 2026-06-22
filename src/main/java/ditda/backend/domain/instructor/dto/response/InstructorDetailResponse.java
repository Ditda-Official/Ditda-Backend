package ditda.backend.domain.instructor.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강사 마이페이지 통계 응답")
public record InstructorDetailResponse(

	@Schema(description = "강사 이름", example = "홍길동")
	String name,

	@Schema(description = "프로필 이미지 URL")
	String profileImageUrl,

	@Schema(description = "강사 외주 통계")
	InstructorStats stats
) {

	@Schema(description = "강사 외주 통계")
	public record InstructorStats(

		@Schema(description = "외주 이용 총 횟수", example = "5")
		long totalCommissionCount,

		@Schema(description = "진행 중인 외주 건수", example = "3")
		long ongoingCommissionCount
	) {
	}
}
