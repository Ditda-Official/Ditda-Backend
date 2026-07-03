package ditda.backend.domain.commission.draft.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.commission.draft.dto.request.DraftSubmitRequest;
import ditda.backend.domain.commission.draft.dto.response.DraftSubmitResponse;
import ditda.backend.domain.commission.draft.facade.DesignerDraftFacade;
import ditda.backend.global.apipayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/designers/commissions")
@RequiredArgsConstructor
@Tag(name = "Designer Draft", description = "디자이너 시안 제출 API")
public class DesignerDraftController {

	private final DesignerDraftFacade designerDraftFacade;

	@Operation(
		summary = "1차 시안 제출",
		description = "**[시안 제출]** presigned PUT URL로 업로드한 S3 임시 key 목록을 전달하여 1차 시안을 제출합니다. "
			+ "파일은 1~9개, 각 30MB 이하, PNG만 허용됩니다.")
	@PostMapping("/{commissionId}/drafts")
	public ApiResponse<DraftSubmitResponse> submitDraft(
		@AuthenticationPrincipal Long designerId,
		@PathVariable Long commissionId,
		@Valid @RequestBody DraftSubmitRequest request
	) {

		DraftSubmitResponse response = designerDraftFacade.submitDraft(designerId, commissionId, request);

		return ApiResponse.onSuccess("1차 시안 제출 성공", response);
	}

}
