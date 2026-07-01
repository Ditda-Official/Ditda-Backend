package ditda.backend.domain.commission.draft.facade;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.CommissionApplication;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.service.CommissionService;
import ditda.backend.domain.commission.draft.dto.request.DraftSubmitRequest;
import ditda.backend.domain.commission.draft.dto.response.DraftSubmitResponse;
import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import ditda.backend.domain.commission.draft.service.DesignerDraftFileService;
import ditda.backend.domain.commission.draft.service.DesignerDraftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DesignerDraftFacade {

	private final CommissionService commissionService;
	private final DesignerDraftService designerDraftService;
	private final DesignerDraftFileService designerDraftFileService;

	// 1차 시안 제출
	@Transactional
	public DraftSubmitResponse submitDraft(
		Long designerId,
		Long commissionId,
		DraftSubmitRequest request
	) {

		// 외주 조회
		Commission commission = commissionService.getByIdForUpdate(commissionId);

		// 디자이너 지원 조회 + ASSIGNED 검증
		CommissionApplication application = designerDraftService
			.findApplicationReadyForDraftSubmission(commissionId, designerId);

		// 파일 검증
		List<String> keys = request.keys();
		designerDraftFileService.validateFiles(keys);

		// 파일 promote 후 영속화
		List<String> permanentKeys = designerDraftFileService.promote(keys);

		CommissionDraft draft;
		try {
			draft = designerDraftService.submitDraft(commission, application, permanentKeys);
		} catch (Exception original) {
			try {
				designerDraftFileService.deleteFiles(permanentKeys);
			} catch (Exception cleanupEx) {
				original.addSuppressed(cleanupEx);
			}
			throw original;
		}

		log.info("디자이너 1차 시안 제출 완료. commissionId={}, designerId={}, draftId={}, fileCount={}",
			commission.getId(), designerId, draft.getId(), permanentKeys.size());

		return new DraftSubmitResponse(commission.getId(), draft.getId(), draft.getCreatedAt());
	}
}
