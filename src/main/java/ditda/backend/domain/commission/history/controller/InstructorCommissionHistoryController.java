package ditda.backend.domain.commission.history.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.commission.history.dto.response.InstructorCommissionHistoryResponse;
import ditda.backend.domain.commission.history.facade.InstructorCommissionHistoryFacade;
import ditda.backend.global.apipayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/instructors/commissions")
@RequiredArgsConstructor
@Tag(name = "Instructor Commission History", description = "강사 외주 내역 API")
public class InstructorCommissionHistoryController {

	private final InstructorCommissionHistoryFacade instructorCommissionHistoryFacade;

	@Operation(summary = "외주 내역 조회", description = "**[마이페이지]** 강사 본인의 전체 외주를 페이지네이션으로 조회합니다.")
	@GetMapping
	public ApiResponse<InstructorCommissionHistoryResponse> getMyCommissions(
		@AuthenticationPrincipal Long instructorId,
		@PageableDefault Pageable pageable
	) {

		// 생성일 기준 내림차순 정렬
		Pageable fixedPageable = PageRequest.of(
			pageable.getPageNumber(),
			pageable.getPageSize(),
			Sort.by(Sort.Direction.DESC, "createdAt")
		);

		InstructorCommissionHistoryResponse response = instructorCommissionHistoryFacade.getInstructorCommissions(
			instructorId,
			fixedPageable
		);
		return ApiResponse.onSuccess("강사 외주 내역 조회 성공", response);
	}
}
