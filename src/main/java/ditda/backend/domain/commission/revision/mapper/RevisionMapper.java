package ditda.backend.domain.commission.revision.mapper;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.domain.commission.draft.exception.DraftErrorCode;
import ditda.backend.domain.commission.revision.dto.response.DesignerRevisionDetailResponse;
import ditda.backend.domain.commission.revision.dto.response.InstructorRevisionDetailResponse;
import ditda.backend.domain.commission.revision.entity.RevisionDetail;
import ditda.backend.domain.commission.revision.entity.RevisionRequest;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.s3.manager.S3PresignedUrlGenerator;
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

		return new InstructorRevisionDetailResponse(
			commission.getId(),
			commission.getTitle(),
			new InstructorRevisionDetailResponse.DraftInfo(draft.getId(), thumbnailUrl, designerComment),
			currentRevisionCount,
			commission.getMaxRevision()
		);
	}

	public DesignerRevisionDetailResponse toDesignerRevisionDetailResponse(
		Commission commission,
		RevisionRequest revisionRequest,
		CommissionDraft draft,
		CommissionDraftFile thumbnail,
		List<RevisionDetail> details,
		int remainingRevisionCount
	) {

		String thumbnailUrl = resolveUrl(thumbnail);

		List<DesignerRevisionDetailResponse.RevisionItem> detailDtos = details.stream()
			.map(d -> new DesignerRevisionDetailResponse.RevisionItem(d.getCategory(), d.getComment()))
			.toList();

		return new DesignerRevisionDetailResponse(
			revisionRequest.getId(),
			commission.getId(),
			commission.getTitle(),
			commission.getFinalDeadline(),
			remainingRevisionCount,
			new DesignerRevisionDetailResponse.TargetDraft(draft.getId(), thumbnailUrl),
			detailDtos
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
