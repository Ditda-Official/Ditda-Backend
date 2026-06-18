package ditda.backend.domain.commission.draft.mapper;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.draft.dto.response.DraftDetailResponse;
import ditda.backend.domain.commission.draft.dto.response.DraftListResponse;
import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.domain.commission.draft.exception.DraftErrorCode;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.s3.S3PresignedUrlGenerator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DraftResponseMapper {

	private final S3PresignedUrlGenerator s3PresignedUrlGenerator;

	public DraftListResponse toDraftListResponse(
		Commission commission,
		List<CommissionDraft> drafts,
		Map<Long, CommissionDraftFile> thumbnailByDraftId
	) {

		List<DraftListResponse.DraftResponse> items = drafts.stream()
			.map(d -> toDraftResponse(d, thumbnailByDraftId.get(d.getId())))
			.toList();

		return new DraftListResponse(commission.getId(), commission.getTitle(), items);
	}

	public DraftDetailResponse toDraftDetailResponse(
		Long commissionId,
		Long draftId,
		List<CommissionDraftFile> files
	) {

		List<DraftDetailResponse.FileResponse> fileResponses = files.stream()
			.map(this::toFileResponse)
			.toList();

		return new DraftDetailResponse(commissionId, draftId, fileResponses);
	}

	private DraftListResponse.DraftResponse toDraftResponse(
		CommissionDraft draft,
		CommissionDraftFile thumbnail
	) {

		return new DraftListResponse.DraftResponse(
			draft.getId(),
			thumbnail == null ? null : resolveUrl(thumbnail),
			thumbnail == null ? null : thumbnail.getWatermarkStatus());
	}

	private DraftDetailResponse.FileResponse toFileResponse(CommissionDraftFile file) {

		return new DraftDetailResponse.FileResponse(
			file.getFileOrder(),
			resolveUrl(file),
			file.getWatermarkStatus()
		);
	}

	private String resolveUrl(CommissionDraftFile file) {

		if (!file.isWatermarkCompleted()) {
			return null;
		}

		String watermarkedFileUrl = file.getWatermarkedFileUrl();
		if (!StringUtils.hasText(watermarkedFileUrl)) {
			throw new GeneralException(DraftErrorCode.WATERMARK_FILE_URL_MISSING);
		}

		return s3PresignedUrlGenerator.generatePrivateGetUrl(watermarkedFileUrl);
	}
}
