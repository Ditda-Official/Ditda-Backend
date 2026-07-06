package ditda.backend.domain.commission.history.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.commission.history.dto.response.InstructorCommissionItemResponse;
import ditda.backend.domain.commission.history.facade.InstructorCommissionHistoryFacade;
import ditda.backend.global.apipayload.request.PageQuery;
import ditda.backend.global.apipayload.response.ApiResponse;
import ditda.backend.global.apipayload.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/instructors/commissions")
@RequiredArgsConstructor
@Validated
@Tag(name = "Instructor", description = "강사 마이페이지 API")
public class InstructorCommissionHistoryController {

	private final InstructorCommissionHistoryFacade instructorCommissionHistoryFacade;

	@Operation(summary = "외주 내역 조회",
		description = "**[마이페이지]** 강사 본인의 전체 외주를 페이지네이션으로 조회합니다. 생성일 최신순으로 정렬됩니다.")
	@GetMapping
	public ApiResponse<PageResponse<InstructorCommissionItemResponse>> getMyCommissions(
		@AuthenticationPrincipal Long instructorId,
		@ParameterObject @Valid PageQuery pageQuery
	) {

		PageResponse<InstructorCommissionItemResponse> response =
			instructorCommissionHistoryFacade.getInstructorCommissions(
				instructorId,
				pageQuery.toPageable()
			);
		return ApiResponse.onSuccess("강사 외주 내역 조회 성공", response);
	}
}
