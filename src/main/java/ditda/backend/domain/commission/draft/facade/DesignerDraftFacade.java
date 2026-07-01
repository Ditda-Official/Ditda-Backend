package ditda.backend.domain.commission.draft.facade;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.CommissionApplication;
import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import ditda.backend.domain.commission.application.service.ApplicationService;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.event.AllFirstDraftsSubmittedEvent;
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

	private static final ZoneId ZONE_KST = ZoneId.of("Asia/Seoul");

	private final CommissionService commissionService;
	private final DesignerDraftService designerDraftService;
	private final DesignerDraftFileService designerDraftFileService;
	private final ApplicationService applicationService;
	private final ApplicationEventPublisher eventPublisher;

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

			// 모든 지원자가 제출을 완료했는지 판단 및 처리
			handleAllSubmittedIfLast(commission);

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

	// 마지막 제출자인지 확인
	private void handleAllSubmittedIfLast(Commission commission) {

		long remainingAssigned = applicationService.countByCommissionAndStatus(
			commission.getId(),
			ApplicationStatus.ASSIGNED
		);

		if (remainingAssigned > 0) {
			return;
		}

		// 마지막 제출자이면 외주 상태 전이 + 강사 알림 이벤트 발행
		commission.startDraftSelecting();
		publishAllFirstDraftsSubmittedEvent(commission.getId());
	}

	// 모든 1차 시안 제출 완료 이벤트 발행
	private void publishAllFirstDraftsSubmittedEvent(Long commissionId) {

		long submittedCount = applicationService.countByCommissionAndStatus(
			commissionId, ApplicationStatus.DRAFT_SUBMITTED);

		Commission commission = commissionService.getWithInstructorAndUserById(commissionId);

		eventPublisher.publishEvent(new AllFirstDraftsSubmittedEvent(
			commission.getId(),
			commission.getTitle(),
			commission.getInstructor().getUser().getEmail(),
			commission.getInstructor().getName(),
			(int)submittedCount,
			LocalDateTime.now(ZONE_KST)
		));

		log.info("모든 1차 시안 제출 완료. commissionId={}, submittedCount={}",
			commissionId, submittedCount);
	}
}
