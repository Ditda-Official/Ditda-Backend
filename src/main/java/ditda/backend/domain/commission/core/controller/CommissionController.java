package ditda.backend.domain.commission.core.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.commission.core.dto.response.PlanListResponse;
import ditda.backend.domain.commission.core.service.CommissionService;
import ditda.backend.global.apipayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/instructors")
@RequiredArgsConstructor
@Tag(name = "Instructor Commission", description = "강사 새 외주 작성 API")
public class CommissionController {

	private final CommissionService commissionService;

	@Operation(summary = "플랜 조회", description = "**[새 외주 작성]** 플랜 정보를 조회합니다.")
	@GetMapping("/plans")
	public ApiResponse<PlanListResponse> getPlans() {
		return ApiResponse.onSuccess("플랜 조회 성공", commissionService.getPlans());
	}
}
