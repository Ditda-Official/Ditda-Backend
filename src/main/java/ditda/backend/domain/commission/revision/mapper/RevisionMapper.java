package ditda.backend.domain.commission.revision.mapper;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.domain.commission.draft.exception.DraftErrorCode;
import ditda.backend.domain.commission.revision.dto.response.InstructorRevisionDetailResponse;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.s3.S3PresignedUrlGenerator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RevisionMapper {

	private final S3PresignedUrlGenerator s3PresignedUrlGenerator;

	public InstructorRevisionDetailResponse toInstructorRevisionDetailResponse(
		Commission commission,
		CommissionDraft draft,
		CommissionDraftFile thumbnail,
		String designerComment,
		int currentRevisionCount
	) {

		String thumbnailUrl = resolveUrl(thumbnail);

		return InstructorRevisionDetailResponse.of(
			commission,
			draft,
			thumbnailUrl,
			designerComment,
			currentRevisionCount
		);
	}

	private String resolveUrl(CommissionDraftFile file) {

		if (file == null || !file.isWatermarkCompleted()) {
			return null;
		}

		String watermarkedFileUrl = file.getWatermarkedFileUrl();
		if (!StringUtils.hasText(watermarkedFileUrl)) {
			throw new GeneralException(DraftErrorCode.WATERMARK_FILE_URL_MISSING);
		}

		return s3PresignedUrlGenerator.generatePrivateGetUrl(watermarkedFileUrl);
	}
}
