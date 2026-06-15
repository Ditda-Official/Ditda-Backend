package ditda.backend.domain.commission.draft.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.exception.CommissionErrorCode;
import ditda.backend.domain.commission.core.repository.CommissionRepository;
import ditda.backend.domain.commission.draft.dto.response.DraftDetailResponse;
import ditda.backend.domain.commission.draft.dto.response.DraftListResponse;
import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.domain.commission.draft.entity.enums.WatermarkStatus;
import ditda.backend.domain.commission.draft.exception.DraftErrorCode;
import ditda.backend.domain.commission.draft.repository.CommissionDraftFileRepository;
import ditda.backend.domain.commission.draft.repository.CommissionDraftRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.s3.S3PresignedUrlGenerator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DraftQueryService {

	private final CommissionRepository commissionRepository;
	private final CommissionDraftRepository commissionDraftRepository;
	private final CommissionDraftFileRepository commissionDraftFileRepository;
	private final S3PresignedUrlGenerator s3PresignedUrlGenerator;

	// 1차 시안 목록 조회
	@Transactional(readOnly = true)
	public DraftListResponse getFirstRoundDrafts(Long instructorId, Long commissionId) {

		// 1. 외주 조회 + 강사 확인
		Commission commission = getOwnedCommission(commissionId, instructorId);

		// 2. 1차 시안 (round = 0) 목록 확인
		List<CommissionDraft> drafts = commissionDraftRepository.findFirstRoundDrafts(commissionId);

		// 마감 전에는 요구 인원이 다 차야 조회 가능, 마감 후에는 덜 차도 조회 가능
		if (!commission.isDraftListViewable(drafts.size(), LocalDate.now())) {
			throw new GeneralException(DraftErrorCode.DRAFTS_NOT_READY);
		}

		// 3. 시안별 썸네일(워터마크) (fileOrder = 0) 목록
		List<Long> draftIds = drafts.stream().map(CommissionDraft::getId).toList();
		Map<Long, CommissionDraftFile> thumbnailByDraftId = commissionDraftFileRepository.findThumbnails(draftIds)
			.stream()
			.collect(Collectors.toMap(f -> f.getCommissionDraft().getId(), f -> f));

		List<DraftListResponse.DraftResponse> responses = drafts.stream()
			.map(d -> {
				CommissionDraftFile thumbnail = thumbnailByDraftId.get(d.getId());
				return new DraftListResponse.DraftResponse(
					d.getId(),
					thumbnail == null ? null : resolveUrl(thumbnail),
					thumbnail == null ? null : thumbnail.getWatermarkStatus()
				);
			})
			.toList();

		return new DraftListResponse(commission.getId(), commission.getTitle(), responses);
	}

	// 1차 시안 상세 조회
	@Transactional(readOnly = true)
	public DraftDetailResponse getDraftDetail(Long instructorId, Long commissionId, Long draftId) {

		// 1. 외주 조회 + 강사 확인
		getOwnedCommission(commissionId, instructorId);

		// 2. 시안 조회
		if (!commissionDraftRepository.existsByIdAndCommissionApplication_Commission_Id(draftId, commissionId)) {
			throw new GeneralException(DraftErrorCode.DRAFT_NOT_FOUND);
		}

		// 3. 1차 시안 상세 파일들(워터마크)
		List<DraftDetailResponse.FileResponse> files = commissionDraftFileRepository
			.findByCommissionDraftIdOrderByFileOrderAsc(draftId).stream()
			.map(f -> new DraftDetailResponse.FileResponse(
				f.getFileOrder(),
				resolveUrl(f),
				f.getWatermarkStatus()))
			.toList();

		return new DraftDetailResponse(commissionId, draftId, files);
	}

	private Commission getOwnedCommission(Long commissionId, Long instructorId) {

		Commission commission = commissionRepository.findById(commissionId)
			.orElseThrow(() -> new GeneralException(CommissionErrorCode.COMMISSION_NOT_FOUND));

		commission.validateOwner(instructorId);
		return commission;
	}

	private String resolveUrl(CommissionDraftFile file) {

		if (file.getWatermarkStatus() != WatermarkStatus.COMPLETED) {
			return null;
		}
		return s3PresignedUrlGenerator.generatePrivateGetUrl(file.getWatermarkedFileUrl());
	}
}
