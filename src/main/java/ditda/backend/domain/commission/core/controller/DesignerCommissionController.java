package ditda.backend.domain.commission.core.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.commission.core.dto.response.CommissionListResponse;
import ditda.backend.domain.commission.core.dto.response.CommissionSummaryResponse;
import ditda.backend.domain.commission.core.facade.DesignerCommissionFacade;
import ditda.backend.global.apipayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/designers/commissions")
@RequiredArgsConstructor
@Tag(name = "Designer Commission", description = "디자이너 외주 조회 API")
public class DesignerCommissionController {

	private final DesignerCommissionFacade designerCommissionFacade;

	@Operation(summary = "모집 중 외주 목록 조회", description = "**[외주 찾기]** 디자이너가 모집 중인 외주 목록을 조회합니다.")
	@GetMapping
	public ApiResponse<CommissionListResponse> getRecruitingCommissions(
		@AuthenticationPrincipal Long designerId,
		@PageableDefault Pageable pageable
	) {

		// 지원 마감일 기준 오름차순 정렬
		Pageable fixedPageable = PageRequest.of(
			pageable.getPageNumber(),
			pageable.getPageSize(),
			Sort.by(Sort.Order.asc("applicationDeadline"), Sort.Order.asc("id"))
		);

		CommissionListResponse response = designerCommissionFacade.getRecruitingCommissionList(
			designerId,
			fixedPageable
		);

		return ApiResponse.onSuccess("디자이너 모집 중 외주 목록 조회 성공", response);
	}

	@Operation(summary = "외주 기본 정보 조회", description = "**[시안 제출]** 디자이너의 시안 제출 페이지에서 표시할 외주 기본 정보를 조회합니다.")
	@GetMapping("/{commissionId}")
	public ApiResponse<CommissionSummaryResponse> getCommissionSummary(
		@AuthenticationPrincipal Long designerId,
		@PathVariable Long commissionId
	) {

		CommissionSummaryResponse response = designerCommissionFacade.getCommissionSummary(designerId, commissionId);

		return ApiResponse.onSuccess("외주 정보 조회 성공", response);
	}
}
