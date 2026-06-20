package ditda.backend.domain.commission.core.facade;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.dto.CommissionFileToSave;
import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest;
import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest.FileInfo;
import ditda.backend.domain.commission.core.dto.request.CommissionFilePresignRequest;
import ditda.backend.domain.commission.core.dto.response.CommissionCreateResponse;
import ditda.backend.domain.commission.core.dto.response.CommissionFilePresignResponse;
import ditda.backend.domain.commission.core.dto.response.PlanListResponse;
import ditda.backend.domain.commission.core.handler.CommissionCategoryHandler;
import ditda.backend.domain.commission.core.service.CommissionCreateFileService;
import ditda.backend.domain.commission.core.service.InstructorCommissionService;
import ditda.backend.domain.commission.core.validator.CommissionCreateValidator;
import ditda.backend.domain.payment.dto.response.DepositNotifyResponse;
import ditda.backend.domain.payment.service.PaymentService;
import ditda.backend.global.s3.PresignedUpload;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InstructorCommissionFacade {

	private final InstructorCommissionService instructorCommissionService;
	private final CommissionCreateFileService commissionCreateFileService;
	private final CommissionCreateValidator commissionCreateValidator;
	private final PaymentService paymentService;

	public PlanListResponse getPlans() {
		return instructorCommissionService.getPlans();
	}

	public CommissionFilePresignResponse issueFilePresignedUrls(CommissionFilePresignRequest request) {

		PresignedUpload presignedUpload = commissionCreateFileService.generatePresignedUpload(
			request.fileKind(),
			request.contentType()
		);

		return new CommissionFilePresignResponse(presignedUpload.key(), presignedUpload.presignedUrl());
	}

	public CommissionCreateResponse createCommission(
		Long instructorId,
		CommissionCreateRequest request
	) {

		// 1. 순수 검증 + 카테고리 핸들러 확보
		CommissionCategoryHandler handler = commissionCreateValidator.validate(request);

		// 2. 파일 검증
		List<FileInfo> files = request.files() == null
			? List.of()
			: request.files();
		commissionCreateFileService.validateFiles(files);

		// 3. promote 후 영속화
		List<CommissionFileToSave> commissionFiles = new ArrayList<>();
		List<String> promotedKeys = new ArrayList<>();
		try {
			for (FileInfo file : files) {
				List<String> permanent = commissionCreateFileService.promote(file.keys());
				commissionFiles.add(new CommissionFileToSave(
					file.fileKind(),
					permanent,
					file.description()
				));
				promotedKeys.addAll(permanent);
			}

			return instructorCommissionService.createCommission(instructorId, request, handler, commissionFiles);
		} catch (Exception original) {
			try {
				commissionCreateFileService.deleteFiles(promotedKeys);
			} catch (Exception cleanupEx) {
				original.addSuppressed(cleanupEx);
			}
			throw original;
		}
	}

	public DepositNotifyResponse notifyDeposit(Long instructorId, Long commissionId) {

		return paymentService.notifyDeposit(instructorId, commissionId);
	}
}
