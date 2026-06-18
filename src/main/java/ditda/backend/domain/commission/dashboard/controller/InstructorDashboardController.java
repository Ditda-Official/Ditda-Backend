package ditda.backend.domain.commission.dashboard.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.commission.dashboard.dto.response.DraftSubmissionCommissionResponse;
import ditda.backend.domain.commission.dashboard.dto.response.MatchingCommissionResponse;
import ditda.backend.domain.commission.dashboard.dto.response.RevisingCommissionResponse;
import ditda.backend.domain.commission.dashboard.service.InstructorDashboardService;
import ditda.backend.global.apipayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/instructors/commissions")
@RequiredArgsConstructor
@Tag(name = "Instructor Dashboard", description = "강사 대시보드 API")
public class InstructorDashboardController {

	private final InstructorDashboardService instructorDashboardService;

	@Operation(summary = "시안 제출 현황 조회", description = "**[대시보드]** 시안 제출 현황을 조회합니다.")
	@GetMapping("/draft-submissions")
	public ApiResponse<DraftSubmissionCommissionResponse> getDraftSubmissions(
		@AuthenticationPrincipal Long instructorId
	) {
		DraftSubmissionCommissionResponse response = instructorDashboardService.getDraftSubmissions(instructorId);

		return ApiResponse.onSuccess("시안 제출 현황 조회 성공", response);
	}

	@Operation(summary = "매칭 중인 외주 조회", description = "**[대시보드]** 매칭 중인 외주를 조회합니다.")
	@GetMapping("/matchings")
	public ApiResponse<MatchingCommissionResponse> getMatchingCommissions(
		@AuthenticationPrincipal Long instructorId
	) {
		MatchingCommissionResponse response = instructorDashboardService.getMatchingCommissions(instructorId);

		return ApiResponse.onSuccess("매칭 중인 외주 조회 성공", response);
	}

	@Operation(summary = "수정 중인 외주 조회", description = "**[대시보드]** 수정 중인 외주를 조회합니다.")
	@GetMapping("/revisions")
	public ApiResponse<RevisingCommissionResponse> getRevisingCommissions(
		@AuthenticationPrincipal Long instructorId
	) {
		RevisingCommissionResponse response = instructorDashboardService.getRevisingCommissions(instructorId);

		return ApiResponse.onSuccess("수정 중인 외주 조회 성공", response);
	}
}
