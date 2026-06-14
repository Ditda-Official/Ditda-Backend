package ditda.backend.domain.commission.draft.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.exception.CommissionErrorCode;
import ditda.backend.domain.commission.core.repository.CommissionRepository;
import ditda.backend.domain.commission.draft.dto.response.DraftListResponse;
import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.domain.commission.draft.exception.DraftErrorCode;
import ditda.backend.domain.commission.draft.repository.CommissionDraftFileRepository;
import ditda.backend.domain.commission.draft.repository.CommissionDraftRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DraftQueryService {

	private final CommissionRepository commissionRepository;
	private final CommissionDraftRepository commissionDraftRepository;
	private final CommissionDraftFileRepository commissionDraftFileRepository;

	// 1차 시안 목록 조회
	@Transactional(readOnly = true)
	public DraftListResponse getFirstRoundDrafts(Long instructorId, Long commissionId) {

		// 1. 외주 조회 + 강사 확인
		Commission commission = commissionRepository.findById(commissionId)
			.orElseThrow(() -> new GeneralException(CommissionErrorCode.COMMISSION_NOT_FOUND));

		if (!Objects.equals(commission.getInstructor().getId(), instructorId)) {
			throw new GeneralException(CommissionErrorCode.COMMISSION_ACCESS_DENIED);
		}

		// 2. 1차 시안 (round = 0) 목록 확인
		List<CommissionDraft> drafts = commissionDraftRepository.findFirstRoundDrafts(commissionId);
		boolean isFirstDeadlinePassed = LocalDate.now().isAfter(commission.getFirstDraftDeadline());
		int required = commission.getPlanCode().getDesignerCount();

		// 마감 전에는 요구 인원이 다 차야 조회 가능, 마감 후에는 덜 차도 조회 가능
		if (!isFirstDeadlinePassed && drafts.size() < required) {
			throw new GeneralException(DraftErrorCode.DRAFTS_NOT_READY);
		}

		// 3. 시안별 썸네일 (fileOrder = 0) 목록
		List<Long> draftIds = drafts.stream().map(CommissionDraft::getId).toList();
		Map<Long, String> thumbnailByDraftId = commissionDraftFileRepository.findThumbnails(draftIds).stream()
			.collect(Collectors.toMap(
				f -> f.getCommissionDraft().getId(),
				CommissionDraftFile::getFileUrl // TODO : 워터마크 도입 후 getWaterMarkedFileUrl()로 교체
			));

		List<DraftListResponse.DraftResponse> responses = drafts.stream()
			.map(d -> new DraftListResponse.DraftResponse(d.getId(), thumbnailByDraftId.get(d.getId())))
			.toList();

		return DraftListResponse.of(commission, responses);
	}
}
