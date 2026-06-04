package ditda.backend.domain.commission.core.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest;
import ditda.backend.domain.commission.core.dto.request.CommissionFilePresignRequest;
import ditda.backend.domain.commission.core.dto.response.CommissionCreateResponse;
import ditda.backend.domain.commission.core.dto.response.CommissionFilePresignResponse;
import ditda.backend.domain.commission.core.dto.response.PlanListResponse;
import ditda.backend.domain.commission.core.facade.CommissionFacade;
import ditda.backend.domain.payment.dto.response.DepositNotifyResponse;
import ditda.backend.global.apipayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/instructors/commissions")
@RequiredArgsConstructor
@Tag(name = "Instructor Commission", description = "강사 새 외주 작성 API")
public class CommissionController {

	private final CommissionFacade commissionFacade;

	@Operation(summary = "플랜 조회", description = "**[새 외주 작성]** 플랜 정보를 조회합니다.")
	@GetMapping("/plans")
	public ApiResponse<PlanListResponse> getPlans() {

		PlanListResponse response = commissionFacade.getPlans();

		return ApiResponse.onSuccess("플랜 조회 성공", response);
	}

	@Operation(
		summary = "새 외주 작성 파일 업로드 URL 발급",
		description = "**[새 외주 작성]** 새 외주 작성시 자료 첨부 및 레퍼런스 파일을 S3에 올릴 presigned PUT URL을 발급합니다. "
			+ "PUT URL로 S3에 직접 업로드한 뒤, 응답의 key를 외주 생성 요청의 keys에 담아 전송합니다."
	)
	@PostMapping("/files/presigned-url")
	public ApiResponse<CommissionFilePresignResponse> issueFilePresignedUrls(
		@Valid @RequestBody CommissionFilePresignRequest request
	) {

		CommissionFilePresignResponse response = commissionFacade.issueFilePresignedUrls(request);

		return ApiResponse.onSuccess("새 외주 작성 파일 업로드 URL 발급 성공", response);
	}

	@Operation(summary = "새 외주 생성", description = "**[새 외주 작성]** 새로운 외주를 생성합니다.")
	@PostMapping
	public ApiResponse<CommissionCreateResponse> createCommission(
		@AuthenticationPrincipal Long instructorId,
		@Valid @RequestBody CommissionCreateRequest request
	) {

		CommissionCreateResponse response = commissionFacade.createCommission(instructorId, request);

		return ApiResponse.onSuccess("외주 생성 성공", response);
	}

	@Operation(summary = "입금 통보", description = "**[새 외주 결제]** 강사가 결제(입금) 완료를 통보하고 관리자에게 확인 메일을 발송합니다.")
	@PostMapping("/{commissionId}/notify-deposit")
	public ApiResponse<DepositNotifyResponse> notifyDeposit(
		@AuthenticationPrincipal Long instructorId,
		@PathVariable Long commissionId
	) {
		DepositNotifyResponse response = commissionFacade.notifyDeposit(instructorId, commissionId);

		return ApiResponse.onSuccess("입금이 접수되었습니다. 관리자 확인 후 진행됩니다.", response);
	}
}
