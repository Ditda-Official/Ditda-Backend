package ditda.backend.domain.commission.dashboard.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.commission.dashboard.dto.response.DesignerAnnouncementCommissionResponse;
import ditda.backend.domain.commission.dashboard.dto.response.DesignerDraftSubmissionCommissionResponse;
import ditda.backend.domain.commission.dashboard.service.DesignerDashboardService;
import ditda.backend.global.apipayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/designers/dashboards")
@RequiredArgsConstructor
@Tag(name = "Designer Dashboard", description = "디자이너 대시보드 API")
public class DesignerDashboardController {

	private final DesignerDashboardService designerDashboardService;

	@Operation(summary = "시안 제출 예정 외주 조회", description = "**[대시보드]** 디자이너가 시안을 제출해야 하는 외주 목록을 조회합니다.")
	@GetMapping("/draft-submissions")
	public ApiResponse<DesignerDraftSubmissionCommissionResponse> getDraftSubmissions(
		@AuthenticationPrincipal Long designerId
	) {
		DesignerDraftSubmissionCommissionResponse response = designerDashboardService.getDraftSubmissions(designerId);

		return ApiResponse.onSuccess("시안 제출 예정 외주 조회 성공", response);
	}

	@Operation(summary = "발표 대기 외주 조회", description = "**[대시보드]** 디자이너가 지원한 외주의 발표 결과를 조회합니다.")
	@GetMapping("/announcements")
	public ApiResponse<DesignerAnnouncementCommissionResponse> getAnnouncements(
		@AuthenticationPrincipal Long designerId
	) {
		DesignerAnnouncementCommissionResponse response = designerDashboardService.getAnnouncements(designerId);

		return ApiResponse.onSuccess("발표 대기 외주 조회 성공", response);
	}
}
