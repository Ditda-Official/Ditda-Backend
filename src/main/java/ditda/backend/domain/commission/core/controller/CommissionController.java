package ditda.backend.domain.commission.core.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.commission.core.dto.response.CommissionDetailResponse;
import ditda.backend.domain.commission.core.facade.CommissionFacade;
import ditda.backend.global.apipayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/commissions")
@RequiredArgsConstructor
@Tag(name = "Commission", description = "외주 상세 조회 API")
public class CommissionController {

	private final CommissionFacade commissionFacade;

	@Operation(summary = "외주 상세 조회", description = "**[외주 조회]** 외주 상세 정보를 조회합니다.")
	@GetMapping("/{commissionId}")
	public ApiResponse<CommissionDetailResponse> getCommissionDetail(
		@AuthenticationPrincipal Long userId,
		@PathVariable Long commissionId
	) {

		CommissionDetailResponse response = commissionFacade.getCommissionDetail(userId, commissionId);

		return ApiResponse.onSuccess("외주 상세 조회 성공", response);
	}
}
