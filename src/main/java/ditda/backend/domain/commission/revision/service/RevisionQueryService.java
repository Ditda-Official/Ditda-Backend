package ditda.backend.domain.commission.revision.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.revision.entity.RevisionDetail;
import ditda.backend.domain.commission.revision.repository.RevisionDetailRepository;
import ditda.backend.domain.commission.revision.repository.RevisionRequestRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RevisionQueryService {

	private final RevisionDetailRepository revisionDetailRepository;
	private final RevisionRequestRepository revisionRequestRepository;

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
}
