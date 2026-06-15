package ditda.backend.domain.commission.draft.mapper;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ditda.backend.domain.commission.draft.dto.response.DraftDetailResponse;
import ditda.backend.domain.commission.draft.dto.response.DraftListResponse;
import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.global.apipayload.code.GeneralErrorCode;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.s3.S3PresignedUrlGenerator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DraftResponseMapper {

	private final S3PresignedUrlGenerator s3PresignedUrlGenerator;

	public DraftListResponse.DraftResponse toDraftResponse(CommissionDraft draft, CommissionDraftFile thumbnail) {

		return new DraftListResponse.DraftResponse(
			draft.getId(),
			thumbnail == null ? null : resolveUrl(thumbnail),
			thumbnail == null ? null : thumbnail.getWatermarkStatus()
		);
	}

	public DraftDetailResponse.FileResponse toFileResponse(CommissionDraftFile file) {

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
			throw new GeneralException(GeneralErrorCode.FILE_URL_GENERATION_FAILED);
		}

		return s3PresignedUrlGenerator.generatePrivateGetUrl(watermarkedFileUrl);
	}
}
