package ditda.backend.domain.commission.draft.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.domain.commission.draft.exception.DraftErrorCode;
import ditda.backend.domain.commission.draft.repository.CommissionDraftFileRepository;
import ditda.backend.domain.commission.draft.repository.CommissionDraftRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DraftQueryService {

	private final CommissionDraftRepository commissionDraftRepository;
	private final CommissionDraftFileRepository commissionDraftFileRepository;

	// 선택된 디자이너의 가장 최근 round 시안 조회
	public CommissionDraft getLatestDraftOfSelectedApplication(Long commissionId) {
		return commissionDraftRepository.findDraftInCommissionByStatus(
				commissionId,
				ApplicationStatus.DRAFT_SELECTED,
				Limit.of(1))
			.orElseThrow(() -> new GeneralException(DraftErrorCode.DRAFT_NOT_FOUND));
	}

	// 외주의 1차 시안 목록
	public List<CommissionDraft> getFirstRoundDrafts(Long commissionId) {
		return commissionDraftRepository.findFirstRoundDrafts(commissionId);
	}

	// 외주의 1차 시안 수
	public int countFirstRoundDrafts(Long commissionId) {
		return commissionDraftRepository.countFirstRoundDrafts(commissionId);
	}

	// 시안 조회
	public CommissionDraft getDraftById(Long draftId) {
		return commissionDraftRepository.findById(draftId)
			.orElseThrow(() -> new GeneralException(DraftErrorCode.DRAFT_NOT_FOUND));
	}

	// 외주에 특정 시안이 존재하는지 조회
	public boolean existsDraftInCommission(Long draftId, Long commissionId) {
		return commissionDraftRepository.existsDraftInCommission(draftId, commissionId);
	}

	// 외주의 특정 시안 조회
	public CommissionDraft getDraftInCommission(Long draftId, Long commissionId) {
		return commissionDraftRepository.findDraftInCommission(draftId, commissionId)
			.orElseThrow(() -> new GeneralException(DraftErrorCode.DRAFT_NOT_FOUND));
	}

	// 시안 목록에 대한 썸네일 Map 조회
	public Map<Long, CommissionDraftFile> getThumbnailsByDraftIds(List<Long> draftIds) {
		if (draftIds.isEmpty()) {
			return Map.of();
		}
		return commissionDraftFileRepository.findThumbnails(draftIds).stream()
			.collect(Collectors.toMap(f -> f.getCommissionDraft().getId(), f -> f));
	}

	// 시안 썸네일 조회
	public CommissionDraftFile findThumbnail(Long draftId) {
		return commissionDraftFileRepository.findThumbnail(draftId).orElse(null);
	}

	// 시안에 속한 파일들 조회
	public List<CommissionDraftFile> getDraftFiles(Long draftId) {
		return commissionDraftFileRepository.findByCommissionDraftIdOrderByFileOrderAsc(draftId);
	}
}
