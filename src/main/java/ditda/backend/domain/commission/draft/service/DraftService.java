package ditda.backend.domain.commission.draft.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.CommissionApplication;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.service.InstructorCommissionService;
import ditda.backend.domain.commission.draft.dto.response.DraftDetailResponse;
import ditda.backend.domain.commission.draft.dto.response.DraftListResponse;
import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.domain.commission.draft.exception.DraftErrorCode;
import ditda.backend.domain.commission.draft.mapper.DraftResponseMapper;
import ditda.backend.domain.commission.draft.repository.CommissionDraftFileRepository;
import ditda.backend.domain.commission.draft.repository.CommissionDraftRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DraftService {

	private final InstructorCommissionService instructorCommissionService;
	private final CommissionDraftRepository commissionDraftRepository;
	private final CommissionDraftFileRepository commissionDraftFileRepository;
	private final DraftResponseMapper draftResponseMapper;

	// 1차 시안 목록 조회
	@Transactional(readOnly = true)
	public DraftListResponse getFirstRoundDrafts(Long instructorId, Long commissionId) {

		// 1. 외주 조회 + 강사 확인
		Commission commission = instructorCommissionService.getOwnedCommission(commissionId, instructorId);

		// 2. 1차 시안 (round = 0) 목록 확인
		List<CommissionDraft> drafts = commissionDraftRepository.findFirstRoundDrafts(commissionId);

		// 마감 전에는 요구 인원이 다 차야 조회 가능, 마감 후에는 덜 차도 조회 가능
		if (!commission.isDraftListViewable(drafts.size(), LocalDate.now())) {
			throw new GeneralException(DraftErrorCode.DRAFTS_NOT_READY);
		}

		Map<Long, CommissionDraftFile> thumbnailByDraftId = drafts.isEmpty()
			? Map.of()
			: commissionDraftFileRepository
			.findThumbnails(drafts.stream().map(CommissionDraft::getId).toList())
			.stream()
			.collect(Collectors.toMap(f -> f.getCommissionDraft().getId(), f -> f));

		return draftResponseMapper.toDraftListResponse(commission, drafts, thumbnailByDraftId);
	}

	// 시안 상세 조회 (1차 시안 및 수정본 포함)
	@Transactional(readOnly = true)
	public DraftDetailResponse getDraftDetail(Long instructorId, Long commissionId, Long draftId) {

		// 1. 외주 조회 + 강사 확인
		Commission commission = instructorCommissionService.getOwnedCommission(commissionId, instructorId);

		CommissionDraft draft = commissionDraftRepository.findById(draftId)
			.orElseThrow(() -> new GeneralException(DraftErrorCode.DRAFT_NOT_FOUND));

		// 1차 시안의 경우에만
		if (draft.isDraftFirstRound()) {
			// 마감 전에는 요구 인원이 다 차야 조회 가능, 마감 후에는 덜 차도 조회 가능
			int draftCount = commissionDraftRepository.countFirstRoundDrafts(commissionId);
			if (!commission.isDraftListViewable(draftCount, LocalDate.now())) {
				throw new GeneralException(DraftErrorCode.DRAFTS_NOT_READY);
			}
		}

		// TODO: 수정본 상세 조회 제약조건 필요

		// 2. 시안 조회
		if (!commissionDraftRepository.existsDraftInCommission(draftId, commissionId)) {
			throw new GeneralException(DraftErrorCode.DRAFT_NOT_FOUND);
		}

		List<CommissionDraftFile> files = commissionDraftFileRepository
			.findByCommissionDraftIdOrderByFileOrderAsc(draftId);

		return draftResponseMapper.toDraftDetailResponse(commissionId, draftId, files);
	}

	// 1차 시안
	public CommissionApplication getApplicationForSelection(Commission commission, Long draftId) {

		// 마감 전에는 요구 인원이 다 차야 조회 가능, 마감 후에는 덜 차도 조회 가능
		int draftCount = commissionDraftRepository.countFirstRoundDrafts(commission.getId());
		if (!commission.isDraftListViewable(draftCount, LocalDate.now())) {
			throw new GeneralException(DraftErrorCode.DRAFTS_NOT_READY);
		}

		CommissionDraft draft = commissionDraftRepository
			.findDraftInCommission(draftId, commission.getId())
			.orElseThrow(() -> new GeneralException(DraftErrorCode.DRAFT_NOT_FOUND));

		if (!draft.isDraftFirstRound()) {
			throw new GeneralException(DraftErrorCode.DRAFT_INVALID_ROUND);
		}

		return draft.getCommissionApplication();
	}
}
