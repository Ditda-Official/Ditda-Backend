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
import ditda.backend.domain.commission.core.service.CommissionCreateFileService;
import ditda.backend.domain.commission.core.service.CommissionService;
import ditda.backend.domain.commission.core.vo.CommissionFileToSave;
import ditda.backend.global.s3.PresignedUpload;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommissionFacade {

	private final CommissionService commissionService;
	private final CommissionCreateFileService commissionCreateFileService;

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

		List<FileInfo> files = request.files() == null
			? List.of()
			: request.files();

		files.forEach(file -> commissionCreateFileService.validateKeys(
			file.fileKind(),
			file.keys()
		));

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

			return commissionService.createCommission(instructorId, request, commissionFiles);
		} catch (Exception exception) {
			commissionCreateFileService.deleteFiles(promotedKeys);
			throw exception;
		}
	}
}
