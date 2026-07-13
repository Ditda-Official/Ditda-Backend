package ditda.backend.domain.commission.application.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.commission.application.facade.DesignerApplicationFacade;
import ditda.backend.global.apipayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/designers/commissions")
@RequiredArgsConstructor
@Tag(name = "Designer Application", description = "디자이너 외주 참여/취소 API")
public class DesignerApplicationController {

	private final DesignerApplicationFacade designerApplicationFacade;

	@Operation(summary = "외주 참여", description = "**[외주 상세 페이지]** 디자이너가 모집 중인 외주에 참여합니다.")
	@PostMapping("/{commissionId}/apply")
	public ApiResponse<Void> applyToCommission(
		@AuthenticationPrincipal Long designerId,
		@PathVariable Long commissionId
	) {

		designerApplicationFacade.apply(designerId, commissionId);

		return ApiResponse.onSuccess("외주 참여 성공");
	}

	@Operation(summary = "외주 참여 취소", description = "**[외주 상세 페이지]** 디자이너가 지원한 외주의 참여를 취소합니다.")
	@PostMapping("/{commissionId}/cancel")
	public ApiResponse<Void> cancelApplication(
		@AuthenticationPrincipal Long designerId,
		@PathVariable Long commissionId
	) {

		designerApplicationFacade.cancel(designerId, commissionId);

		return ApiResponse.onSuccess("외주 참여 취소 성공");
	}
}
