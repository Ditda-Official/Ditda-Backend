package ditda.backend.domain.commission.revision.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.commission.revision.dto.request.RevisionCreateRequest;
import ditda.backend.domain.commission.revision.dto.response.InstructorRevisionDetailResponse;
import ditda.backend.domain.commission.revision.facade.InstructorRevisionFacade;
import ditda.backend.global.apipayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/instructors/commissions")
@RequiredArgsConstructor
@Tag(name = "Instructor Revision", description = "강사 시안 수정 API")
public class InstructorRevisionController {

	private final InstructorRevisionFacade instructorRevisionFacade;

	@Operation(summary = "수정 시안 상세 조회", description = "**[수정]** 수정 중인 시안 정보를 조회합니다.")
	@GetMapping("/{commissionId}/revisions/current")
	public ApiResponse<InstructorRevisionDetailResponse> getRevisionDetail(
		@AuthenticationPrincipal Long instructorId,
		@PathVariable Long commissionId
	) {
		InstructorRevisionDetailResponse response = instructorRevisionFacade.getRevisionDetail(
			instructorId,
			commissionId
		);

		return ApiResponse.onSuccess("시안 상세 정보 조회 성공", response);
	}

	@Operation(summary = "시안 수정 요청 생성", description = "**[수정]** 시안 수정 요청을 생성합니다.")
	@PostMapping("/{commissionId}/revisions")
	public ApiResponse<Void> createRevision(
		@AuthenticationPrincipal Long instructorId,
		@PathVariable Long commissionId,
		@Valid @RequestBody RevisionCreateRequest request
	) {
		instructorRevisionFacade.createRevision(instructorId, commissionId, request);

		return ApiResponse.onSuccess("수정 요청이 전달되었습니다.");
	}
}
