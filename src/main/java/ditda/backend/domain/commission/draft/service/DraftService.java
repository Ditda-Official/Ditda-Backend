package ditda.backend.domain.commission.draft.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.CommissionApplication;
import ditda.backend.domain.commission.application.repository.CommissionApplicationRepository;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.exception.CommissionErrorCode;
import ditda.backend.domain.commission.core.repository.CommissionRepository;
import ditda.backend.domain.commission.draft.dto.response.DraftDetailResponse;
import ditda.backend.domain.commission.draft.dto.response.DraftListResponse;
import ditda.backend.domain.commission.draft.dto.response.DraftSelectResponse;
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

	private static final int EXP_ON_SELECTION = 150;

	private final CommissionRepository commissionRepository;
	private final CommissionDraftRepository commissionDraftRepository;
	private final CommissionDraftFileRepository commissionDraftFileRepository;
	private final CommissionApplicationRepository commissionApplicationRepository;
	private final DraftResponseMapper draftResponseMapper;

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
		getOwnedCommission(commissionId, instructorId);

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

	// 1차 시안 확정
	@Transactional
	public DraftSelectResponse selectDraft(Long instructorId, Long commissionId, Long draftId) {

		// 1. 외주 조회 + 강사 확인
		Commission commission = getOwnedCommission(commissionId, instructorId);

		// 2. 외주 상태 확인
		if (commission.isDesignerSelected()) {
			throw new GeneralException(CommissionErrorCode.DESIGNER_ALREADY_SELECTED);
		}

		if (!commission.isSelectable()) {
			throw new GeneralException(CommissionErrorCode.COMMISSION_STATUS_INVALID);
		}

		// 3. 시안 조회 + 1차 시안 확인
		CommissionDraft draft = commissionDraftRepository
			.findByIdAndCommissionApplication_Commission_Id(draftId, commissionId)
			.orElseThrow(() -> new GeneralException(DraftErrorCode.DRAFT_NOT_FOUND));
		if (!draft.isDraftFirstRound()) {
			throw new GeneralException(DraftErrorCode.DRAFT_INVALID_ROUND);
		}

		// 4. 시안 선택
		CommissionApplication selected = draft.getCommissionApplication();
		commission.selectDesigner(selected.getDesigner(), LocalDateTime.now());

		selected.markDraftSelected();
		selected.getDesigner().gainExp(EXP_ON_SELECTION);

		commissionApplicationRepository.findByCommission_Id(commissionId).stream()
			.filter(app -> !app.getId().equals(selected.getId()))
			.forEach(CommissionApplication::markDraftRejected);

		return new DraftSelectResponse(
			commissionId,
			draftId,
			selected.getStatus(),
			commission.getMaxRevision(),
			commission.getSelectedAt()
		);
	}

	private Commission getOwnedCommission(Long commissionId, Long instructorId) {

		Commission commission = commissionRepository.findById(commissionId)
			.orElseThrow(() -> new GeneralException(CommissionErrorCode.COMMISSION_NOT_FOUND));

		if (!commission.isOwnedBy(instructorId)) {
			throw new GeneralException(CommissionErrorCode.COMMISSION_ACCESS_DENIED);
		}
		return commission;
	}
}
