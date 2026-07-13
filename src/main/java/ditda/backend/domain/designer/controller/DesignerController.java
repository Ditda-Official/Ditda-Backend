package ditda.backend.domain.designer.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.designer.dto.response.DesignerStatsResponse;
import ditda.backend.domain.designer.facade.DesignerFacade;
import ditda.backend.global.apipayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/designers")
@RequiredArgsConstructor
@Tag(name = "Designer", description = "디자이너 마이페이지 API")
public class DesignerController {

	private final DesignerFacade designerFacade;

	@Operation(summary = "디자이너 마이페이지 통계 조회", description = "**[마이페이지]** 디자이너 본인 정보와 외주 통계를 조회합니다.")
	@GetMapping("/me")
	public ApiResponse<DesignerStatsResponse> getMyDetail(
		@AuthenticationPrincipal Long designerId
	) {
		return ApiResponse.onSuccess("디자이너 마이페이지 통계 조회 성공", designerFacade.getDesignerStats(designerId));
	}
}
