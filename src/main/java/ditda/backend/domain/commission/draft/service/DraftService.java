package ditda.backend.domain.commission.draft.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.CommissionApplication;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.service.CommissionService;
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

	private final CommissionService commissionService;
	private final CommissionDraftRepository commissionDraftRepository;
	private final CommissionDraftFileRepository commissionDraftFileRepository;
	private final DraftResponseMapper draftResponseMapper;

	// 1차 시안 목록 조회
	@Transactional(readOnly = true)
	public DraftListResponse getFirstRoundDrafts(Long instructorId, Long commissionId) {

		// 1. 외주 조회 + 강사 확인
		Commission commission = commissionService.getOwnedCommission(commissionId, instructorId);

		// 2. 1차 시안 (round = 0) 목록 확인
		List<CommissionDraft> drafts = commissionDraftRepository.findFirstRoundDrafts(commissionId);

		// 마감 전에는 요구 인원이 다 차야 조회 가능, 마감 후에는 덜 차도 조회 가능
		if (!commission.isDraftListViewable(drafts.size(), LocalDate.now())) {
			throw new GeneralException(DraftErrorCode.DRAFTS_NOT_READY);
		}

		if (drafts.isEmpty()) {
			return new DraftListResponse(commission.getId(), commission.getTitle(), List.of());
		}

		// 3. 시안별 썸네일(워터마크) (fileOrder = 0) 목록
		List<Long> draftIds = drafts.stream().map(CommissionDraft::getId).toList();
		Map<Long, CommissionDraftFile> thumbnailByDraftId = commissionDraftFileRepository.findThumbnails(draftIds)
			.stream()
			.collect(Collectors.toMap(f -> f.getCommissionDraft().getId(), f -> f));

		List<DraftListResponse.DraftResponse> responses = drafts.stream()
			.map(d -> draftResponseMapper.toDraftResponse(d, thumbnailByDraftId.get(d.getId())))
			.toList();

		return new DraftListResponse(commission.getId(), commission.getTitle(), responses);
	}

	// 시안 상세 조회
	@Transactional(readOnly = true)
	public DraftDetailResponse getDraftDetail(Long instructorId, Long commissionId, Long draftId) {

		// 1. 외주 조회 + 강사 확인
		commissionService.getOwnedCommission(commissionId, instructorId);

		// 2. 시안 조회
		if (!commissionDraftRepository.existsByIdAndCommissionApplication_Commission_Id(draftId, commissionId)) {
			throw new GeneralException(DraftErrorCode.DRAFT_NOT_FOUND);
		}

		// 3. 시안 상세 파일들(워터마크)
		List<DraftDetailResponse.FileResponse> files = commissionDraftFileRepository
			.findByCommissionDraftIdOrderByFileOrderAsc(draftId).stream()
			.map(draftResponseMapper::toFileResponse)
			.toList();

		return new DraftDetailResponse(commissionId, draftId, files);
	}

	// 1차 시안
	public CommissionApplication getApplicationForSelection(Long commissionId, Long draftId) {

		CommissionDraft draft = commissionDraftRepository
			.findByIdAndCommissionApplication_Commission_Id(draftId, commissionId)
			.orElseThrow(() -> new GeneralException(DraftErrorCode.DRAFT_NOT_FOUND));

		if (!draft.isDraftFirstRound()) {
			throw new GeneralException(DraftErrorCode.DRAFT_INVALID_ROUND);
		}

		return draft.getCommissionApplication();
	}
}
