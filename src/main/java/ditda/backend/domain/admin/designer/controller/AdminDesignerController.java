package ditda.backend.domain.admin.designer.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.admin.designer.dto.response.DesignerAccountResponse;
import ditda.backend.domain.admin.designer.service.AdminDesignerService;
import ditda.backend.global.apipayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/designers")
@RequiredArgsConstructor
@Tag(name = "Admin Designer", description = "어드민 디자이너 관리 API")
public class AdminDesignerController {

	private final AdminDesignerService adminDesignerService;

	@Operation(summary = "디자이너 계좌 정보 조회", description = "**[어드민]** 정산 처리를 위해 디자이너의 계좌 정보를 조회합니다.")
	@GetMapping("/{designerId}/account")
	public ApiResponse<DesignerAccountResponse> getDesignerAccount(
		@AuthenticationPrincipal Long adminId,
		@PathVariable Long designerId
	) {

		DesignerAccountResponse response = adminDesignerService.getDesignerAccount(adminId, designerId);

		return ApiResponse.onSuccess("디자이너 계좌 정보 조회 성공", response);
	}
}
