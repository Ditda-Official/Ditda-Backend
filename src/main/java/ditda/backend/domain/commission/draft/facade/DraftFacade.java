package ditda.backend.domain.commission.draft.facade;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.CommissionApplication;
import ditda.backend.domain.commission.application.service.ApplicationService;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.service.CommissionService;
import ditda.backend.domain.commission.draft.dto.response.DraftDetailResponse;
import ditda.backend.domain.commission.draft.dto.response.DraftListResponse;
import ditda.backend.domain.commission.draft.dto.response.DraftSelectResponse;
import ditda.backend.domain.commission.draft.service.DraftService;
import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.domain.designer.entity.enums.ExpReward;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DraftFacade {

	private final CommissionService commissionService;
	private final DraftService draftService;
	private final ApplicationService applicationService;

	// 1차 시안 목록 조회
	public DraftListResponse getFirstRoundDrafts(Long instructorId, Long commissionId) {
		return draftService.getFirstRoundDrafts(instructorId, commissionId);
	}

	// 시안 상세 조회
	public DraftDetailResponse getDraftDetail(Long instructorId, Long commissionId, Long draftId) {
		return draftService.getDraftDetail(instructorId, commissionId, draftId);
	}

	// 1차 시안 선택
	@Transactional
	public DraftSelectResponse selectDraft(Long instructorId, Long commissionId, Long draftId) {

		Commission commission = commissionService.getOwnedCommission(commissionId, instructorId);

		CommissionApplication selected = draftService.getApplicationForSelection(commissionId, draftId);
		Designer designer = selected.getDesigner();

		LocalDateTime now = LocalDateTime.now();
		commissionService.selectDesigner(commission, designer, now);

		applicationService.applySelection(commissionId, selected.getId());

		designer.gainExp(ExpReward.DRAFT_SELECTED.getAmount());

		return new DraftSelectResponse(
			commissionId,
			draftId,
			selected.getStatus(),
			commission.getMaxRevision(),
			now
		);
	}
}
