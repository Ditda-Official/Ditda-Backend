package ditda.backend.domain.commission.revision.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import ditda.backend.domain.commission.revision.dto.request.RevisionCreateRequest;
import ditda.backend.domain.commission.revision.entity.RevisionDetail;
import ditda.backend.domain.commission.revision.entity.RevisionRequest;
import ditda.backend.domain.commission.revision.repository.RevisionDetailRepository;
import ditda.backend.domain.commission.revision.repository.RevisionRequestRepository;
import ditda.backend.domain.commission.revision.repository.RevisionResponseRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RevisionService {

	private final RevisionRequestRepository revisionRequestRepository;
	private final RevisionResponseRepository revisionResponseRepository;
	private final RevisionDetailRepository revisionDetailRepository;

	// 시안에 달린 디자이너 코멘트 조회 + 확인 처리
	@Transactional
	public String getDesignerCommentAndCheck(Long draftId) {
		return revisionResponseRepository.findByProducedDraftId(draftId)
			.map(response -> {
				response.check();
				return response.getDesignerComment();
			})
			.orElse(null);
	}

	// 현재 차수 계산 (0부터 시작)
	public int calculateCurrentRevisionCount(Commission commission) {
		int used = revisionRequestRepository.countByCommissionId(commission.getId());

		return Math.min(used, commission.getMaxRevision());
	}

	// 수정 요청이 존재하는지 여부
	public boolean hasRevisionRequestOnDraft(Long draftId) {
		return revisionRequestRepository.existsByTargetDraftId(draftId);
	}

	@Transactional
	public void createRevisionRequest(Commission commission, CommissionDraft draft, RevisionCreateRequest request) {

		// 시안 수정 요청 저장
		RevisionRequest revisionRequest = RevisionRequest.create(commission, draft);
		RevisionRequest savedRevisionRequest = revisionRequestRepository.save(revisionRequest);

		// 시안 수정 요청 카테고리 저장
		List<RevisionDetail> revisionDetails = request.categories().stream()
			.map(d -> RevisionDetail.create(savedRevisionRequest, d.category(), d.comment()))
			.toList();
		revisionDetailRepository.saveAll(revisionDetails);
	}
}
