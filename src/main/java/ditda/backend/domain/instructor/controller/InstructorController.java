package ditda.backend.domain.instructor.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.instructor.dto.response.InstructorStatsResponse;
import ditda.backend.domain.instructor.facade.InstructorFacade;
import ditda.backend.global.apipayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/instructors")
@RequiredArgsConstructor
@Tag(name = "Instructor", description = "강사 마이페이지 API")
public class InstructorController {

	private final InstructorFacade instructorFacade;

	@Operation(summary = "강사 마이페이지 통계 조회", description = "**[마이페이지]** 강사 본인 정보와 외주 통계를 조회합니다.")
	@GetMapping("/me")
	public ApiResponse<InstructorStatsResponse> getMyDetail(
		@AuthenticationPrincipal Long instructorId
	) {
		return ApiResponse.onSuccess("강사 마이페이지 통계 조회 성공", instructorFacade.getMyStats(instructorId));
	}
}
