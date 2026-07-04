package ditda.backend.domain.settlement.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.settlement.dto.response.DesignerSettlementResponse;
import ditda.backend.domain.settlement.facade.DesignerSettlementFacade;
import ditda.backend.global.apipayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/designers/settlements")
@RequiredArgsConstructor
@Tag(name = "Designer", description = "디자이너 마이페이지 API")
public class SettlementController {

	private final DesignerSettlementFacade designerSettlementFacade;

	@Operation(summary = "디자이너 지급 내역 조회",
		description = "**[마이페이지]** 디자이너 본인의 지급 내역을 페이지네이션으로 조회합니다. 지급 일시 최신순으로 정렬됩니다.")
	@GetMapping
	public ApiResponse<DesignerSettlementResponse> getMyPayments(
		@AuthenticationPrincipal Long designerId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {

		return ApiResponse.onSuccess(
			"디자이너 지급 내역 조회 성공",
			designerSettlementFacade.getDesignerSettlements(
				designerId,
				PageRequest.of(page, size)
			)
		);
	}
}
