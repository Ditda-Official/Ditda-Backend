package ditda.backend.domain.commission.revision.facade;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.service.DesignerCommissionService;
import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.domain.commission.draft.service.DraftQueryService;
import ditda.backend.domain.commission.revision.dto.response.DesignerRevisionDetailResponse;
import ditda.backend.domain.commission.revision.entity.RevisionDetail;
import ditda.backend.domain.commission.revision.entity.RevisionRequest;
import ditda.backend.domain.commission.revision.mapper.RevisionMapper;
import ditda.backend.domain.commission.revision.service.DesignerRevisionService;
import ditda.backend.domain.commission.revision.service.RevisionQueryService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DesignerRevisionFacade {

	private final DesignerCommissionService designerCommissionService;
	private final DraftQueryService draftQueryService;
	private final RevisionQueryService revisionQueryService;
	private final DesignerRevisionService designerRevisionService;
	private final RevisionMapper revisionMapper;

	@Transactional
	public DesignerRevisionDetailResponse getRevisionDetail(Long designerId, Long commissionId) {

		// 외주 조회 + 디자이너 확인
		Commission commission = designerCommissionService.getSelectedCommission(commissionId, designerId);

		// 수정 단계 확인
		commission.validateRevisable();

		// 가장 최근 시안
		CommissionDraft latestDraft = draftQueryService.getLatestDraftOfSelectedApplication(commissionId);

		// 수정 요청 조회 + 확인 처리
		RevisionRequest revisionRequest = designerRevisionService.getRevisionRequestAndCheck(latestDraft.getId());

		// 수정 요청 항목
		List<RevisionDetail> details = revisionQueryService.getRevisionDetails(revisionRequest.getId());

		// 썸네일
		CommissionDraftFile thumbnail = draftQueryService.findThumbnail(latestDraft.getId());

		int currentRevisionCount = revisionQueryService.calculateCurrentRevisionCount(commission);
		int remainingRevisionCount = commission.getRemainingRevisionCount(currentRevisionCount);

		return revisionMapper.toDesignerRevisionDetailResponse(
			commission,
			revisionRequest,
			latestDraft,
			thumbnail,
			details,
			remainingRevisionCount
		);
	}
}
