package ditda.backend.domain.commission.revision.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.revision.entity.RevisionResponse;
import ditda.backend.domain.commission.revision.repository.RevisionRequestRepository;
import ditda.backend.domain.commission.revision.repository.RevisionResponseRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RevisionService {

	private final RevisionRequestRepository revisionRequestRepository;
	private final RevisionResponseRepository revisionResponseRepository;

	// 시안에 달린 디자이너 코멘트 조회
	public String getDesignerComment(Long draftId) {
		return revisionResponseRepository.findByProducedDraftId(draftId)
			.map(RevisionResponse::getDesignerComment)
			.orElse(null);
	}

	// 현재 차수 계산 (0부터 시작)
	public int calculateCurrentRevisionCount(Commission commission) {
		int used = revisionRequestRepository.countByCommissionId(commission.getId());

		return Math.min(used, commission.getMaxRevision());
	}
}
