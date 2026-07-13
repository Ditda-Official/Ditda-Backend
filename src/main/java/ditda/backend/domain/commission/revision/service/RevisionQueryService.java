package ditda.backend.domain.commission.revision.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.revision.entity.RevisionDetail;
import ditda.backend.domain.commission.revision.entity.RevisionRequest;
import ditda.backend.domain.commission.revision.exception.RevisionErrorCode;
import ditda.backend.domain.commission.revision.repository.RevisionDetailRepository;
import ditda.backend.domain.commission.revision.repository.RevisionRequestRepository;
import ditda.backend.domain.commission.revision.repository.RevisionResponseRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RevisionQueryService {

	private final RevisionDetailRepository revisionDetailRepository;
	private final RevisionRequestRepository revisionRequestRepository;
	private final RevisionResponseRepository revisionResponseRepository;

	// 수정 내용 조회
	public List<RevisionDetail> getRevisionDetails(Long revisionRequestId) {
		return revisionDetailRepository.findAllByRevisionRequest_Id(revisionRequestId);
	}

	// 현재 차수 계산 (0부터 시작)
	public int calculateCurrentRevisionCount(Commission commission) {
		return revisionRequestRepository.countByCommissionId(commission.getId());
	}

	// 수정 요청이 존재하는지 여부
	public boolean hasRevisionRequestOnDraft(Long draftId) {
		return revisionRequestRepository.existsByTargetDraftId(draftId);
	}

	// 시안에 달린 수정 요청 조회
	public RevisionRequest getRevisionRequestOnDraft(Long draftId) {
		return revisionRequestRepository.findByTargetDraftId(draftId)
			.orElseThrow(() -> new GeneralException(RevisionErrorCode.REVISION_REQUEST_NOT_FOUND));
	}

	// 수정 요청에 대한 답변 존재 여부
	public boolean hasRevisionResponse(Long revisionRequestId) {
		return revisionResponseRepository.existsById(revisionRequestId);
	}
}
