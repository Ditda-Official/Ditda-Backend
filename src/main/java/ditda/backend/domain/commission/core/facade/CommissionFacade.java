package ditda.backend.domain.commission.core.facade;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest;
import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest.FileInfo;
import ditda.backend.domain.commission.core.dto.request.CommissionFilePresignRequest;
import ditda.backend.domain.commission.core.dto.response.CommissionCreateResponse;
import ditda.backend.domain.commission.core.dto.response.CommissionFilePresignResponse;
import ditda.backend.domain.commission.core.dto.response.PlanListResponse;
import ditda.backend.domain.commission.core.handler.CommissionCategoryHandler;
import ditda.backend.domain.commission.core.service.CommissionCreateFileService;
import ditda.backend.domain.commission.core.service.CommissionService;
import ditda.backend.domain.commission.core.validator.CommissionCreateValidator;
import ditda.backend.domain.commission.core.vo.CommissionFileToSave;
import ditda.backend.global.s3.PresignedUpload;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommissionFacade {

	private final CommissionService commissionService;
	private final CommissionCreateFileService commissionCreateFileService;
	private final CommissionCreateValidator commissionCreateValidator;

	public PlanListResponse getPlans() {
		return commissionService.getPlans();
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

			return commissionService.createCommission(instructorId, request, handler, commissionFiles);
		} catch (Exception exception) {
			commissionCreateFileService.deleteFiles(promotedKeys);
			throw exception;
		}
	}
}
