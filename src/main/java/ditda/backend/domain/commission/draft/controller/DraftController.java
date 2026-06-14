package ditda.backend.domain.commission.draft.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.commission.draft.dto.response.DraftListResponse;
import ditda.backend.domain.commission.draft.service.DraftQueryService;
import ditda.backend.global.apipayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/instructors/commissions")
@RequiredArgsConstructor
@Tag(name = "Instructor Draft", description = "강사 시안 조회 API")
public class DraftController {

	private final DraftQueryService draftQueryService;

	@Operation(summary = "1차 시안 목록 조회", description = "**[시안 제출 조회]** 외주명 클릭 시 디자이너들이 제출한 1차 시안 목록을 조회합니다.")
	@GetMapping("/{commissionId}/drafts")
	public ApiResponse<DraftListResponse> getFirstRoundDrafts(
		@AuthenticationPrincipal Long instructorId,
		@PathVariable Long commissionId
	) {

		DraftListResponse response = draftQueryService.getFirstRoundDrafts(instructorId, commissionId);
		return ApiResponse.onSuccess("1차 시안 목록 조회 성공", response);
	}
}
