package ditda.backend.domain.commission.core.dto.response;

import java.util.Arrays;
import java.util.List;

import ditda.backend.domain.commission.core.entity.enums.PlanCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플랜 목록 조회 응답")
public record PlanListResponse(
	List<PlanResponse> plans
) {

	public static PlanListResponse from() {
		List<PlanResponse> plans = Arrays.stream(PlanCode.values())
			.map(PlanResponse::from)
			.toList();

		return new PlanListResponse(plans);
	}

	@Schema(description = "플랜 정보")
	public record PlanResponse(

		@Schema(description = "플랜 코드", example = "BASIC")
		String code,

		@Schema(description = "디자이너 수", example = "3")
		int designerCount,

		@Schema(description = "가격", example = "450000")
		int price,

		@Schema(description = "상세 내용", example = "디자이너 3명에 대한 시안을 받아볼 수 있습니다.")
		String description
	) {
		public static PlanResponse from(PlanCode planCode) {
			return new PlanResponse(
				planCode.name(),
				planCode.getDesignerCount(),
				planCode.getPrice(),
				planCode.getDescription()
			);
		}
	}
}
