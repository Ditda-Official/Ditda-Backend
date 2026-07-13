package ditda.backend.domain.commission.revision.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import ditda.backend.domain.commission.revision.entity.RevisionRequest;
import ditda.backend.domain.commission.revision.entity.RevisionResponse;
import ditda.backend.domain.commission.revision.exception.RevisionErrorCode;
import ditda.backend.domain.commission.revision.repository.RevisionRequestRepository;
import ditda.backend.domain.commission.revision.repository.RevisionResponseRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DesignerRevisionService {

	private final RevisionRequestRepository revisionRequestRepository;
	private final RevisionResponseRepository revisionResponseRepository;

	// 시안 수정에 달린 요청 조회 + 확인 처리
	@Transactional
	public RevisionRequest getRevisionRequestAndCheck(Long draftId) {
		RevisionRequest request = revisionRequestRepository.findByTargetDraftId(draftId)
			.orElseThrow(() -> new GeneralException(RevisionErrorCode.REVISION_REQUEST_NOT_FOUND));
		request.check();
		return request;
	}

	// 수정 답변 저장
	@Transactional
	public void createRevisionResponse(
		RevisionRequest revisionRequest,
		CommissionDraft draft,
		String designerComment
	) {

		revisionResponseRepository.save(
			RevisionResponse.create(revisionRequest, draft, designerComment)
		);
	}
}
