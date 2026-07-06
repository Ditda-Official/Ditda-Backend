package ditda.backend.domain.commission.revision.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.commission.revision.dto.request.RevisionSubmitRequest;
import ditda.backend.domain.commission.revision.dto.response.DesignerRevisionDetailResponse;
import ditda.backend.domain.commission.revision.dto.response.RevisionSubmitResponse;
import ditda.backend.domain.commission.revision.facade.DesignerRevisionFacade;
import ditda.backend.global.apipayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/designers/commissions")
@RequiredArgsConstructor
@Tag(name = "Designer Revision", description = "디자이너 시안 수정 API")
public class DesignerRevisionController {

	private final DesignerRevisionFacade designerRevisionFacade;

	@Operation(summary = "수정 요청 사항 조회", description = "**[수정]** 진행 중 외주의 수정 요청 사항을 조회합니다.")
	@GetMapping("/{commissionId}/revisions/current")
	public ApiResponse<DesignerRevisionDetailResponse> getRevisionDetail(
		@AuthenticationPrincipal Long designerId,
		@PathVariable Long commissionId
	) {
		DesignerRevisionDetailResponse response =
			designerRevisionFacade.getRevisionDetail(designerId, commissionId);
		return ApiResponse.onSuccess("수정 요청 사항 조회 성공", response);
	}

	@Operation(summary = "수정본 제출",
		description = "**[수정]** 선택된 외주에 대한 현재 수정 요청의 수정본 파일 및 커멘트를 제출합니다.")
	@PostMapping("/{commissionId}/revisions/current")
	public ApiResponse<RevisionSubmitResponse> submitRevision(
		@AuthenticationPrincipal Long designerId,
		@PathVariable Long commissionId,
		@RequestBody @Valid RevisionSubmitRequest request
	) {

		RevisionSubmitResponse response = designerRevisionFacade.submitRevision(designerId, commissionId, request);
		return ApiResponse.onSuccess("수정본 제출 성공", response);
	}

}
