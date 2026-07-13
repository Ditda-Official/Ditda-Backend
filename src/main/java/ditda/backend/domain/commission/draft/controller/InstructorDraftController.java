package ditda.backend.domain.commission.draft.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.commission.draft.dto.response.DraftDetailResponse;
import ditda.backend.domain.commission.draft.dto.response.DraftListResponse;
import ditda.backend.domain.commission.draft.dto.response.DraftSelectResponse;
import ditda.backend.domain.commission.draft.facade.InstructorDraftFacade;
import ditda.backend.global.apipayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/instructors/commissions")
@RequiredArgsConstructor
@Tag(name = "Instructor Draft", description = "강사 시안 조회/선택 API")
public class InstructorDraftController {

	private final InstructorDraftFacade instructorDraftFacade;

	@Operation(summary = "1차 시안 목록 조회", description = "**[시안 제출 조회]** 외주명 클릭 시 디자이너들이 제출한 1차 시안 목록을 조회합니다.")
	@GetMapping("/{commissionId}/drafts")
	public ApiResponse<DraftListResponse> getFirstRoundDrafts(
		@AuthenticationPrincipal Long instructorId,
		@PathVariable Long commissionId
	) {

		DraftListResponse response = instructorDraftFacade.getFirstRoundDrafts(instructorId, commissionId);
		return ApiResponse.onSuccess("1차 시안 목록 조회 성공", response);
	}

	@Operation(summary = "시안 상세 조회", description = "**[시안 상세 조회]** 시안 클릭 시 시안의 상세 정보를 반환합니다.")
	@GetMapping("/{commissionId}/drafts/{draftId}")
	public ApiResponse<DraftDetailResponse> getDraftDetail(
		@AuthenticationPrincipal Long instructorId,
		@PathVariable Long commissionId,
		@PathVariable Long draftId
	) {

		DraftDetailResponse response = instructorDraftFacade.getDraftDetail(instructorId, commissionId, draftId);
		return ApiResponse.onSuccess("시안 상세 조회 성공", response);
	}

	@Operation(summary = "1차 시안 선택", description = "**[시안 확인]** 시안 선택 후 제출하기 클릭 시 해당 시안의 디자이너로 확정하고 외주를 수정 단계로 전환합니다.")
	@PostMapping("/{commissionId}/drafts/{draftId}/select")
	public ApiResponse<DraftSelectResponse> selectDraft(
		@AuthenticationPrincipal Long instructorId,
		@PathVariable Long commissionId,
		@PathVariable Long draftId
	) {

		DraftSelectResponse response = instructorDraftFacade.selectDraft(instructorId, commissionId, draftId);
		return ApiResponse.onSuccess("1차 시안 선택 성공", response);
	}

	@Operation(summary = "외주 최종 확정", description = "**[수정]** 강사가 현재 시안을 최종 시안으로 확정합니다.")
	@PostMapping("/{commissionId}/drafts/{draftId}/finalize")
	public ApiResponse<Void> finalizeDraft(
		@AuthenticationPrincipal Long instructorId,
		@PathVariable Long commissionId,
		@PathVariable Long draftId
	) {
		instructorDraftFacade.finalizeDraft(instructorId, commissionId, draftId);

		return ApiResponse.onSuccess("외주가 최종 확정되었습니다.");
	}
}
