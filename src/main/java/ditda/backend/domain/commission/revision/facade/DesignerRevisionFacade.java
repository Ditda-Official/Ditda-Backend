package ditda.backend.domain.commission.revision.facade;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.CommissionApplication;
import ditda.backend.domain.commission.application.service.ApplicationService;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.event.RevisionSubmittedEvent;
import ditda.backend.domain.commission.core.service.CommissionService;
import ditda.backend.domain.commission.core.service.DesignerCommissionService;
import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.domain.commission.draft.service.DesignerDraftFileService;
import ditda.backend.domain.commission.draft.service.DesignerDraftService;
import ditda.backend.domain.commission.draft.service.DraftQueryService;
import ditda.backend.domain.commission.revision.dto.request.RevisionSubmitRequest;
import ditda.backend.domain.commission.revision.dto.response.DesignerRevisionDetailResponse;
import ditda.backend.domain.commission.revision.dto.response.RevisionSubmitResponse;
import ditda.backend.domain.commission.revision.entity.RevisionDetail;
import ditda.backend.domain.commission.revision.entity.RevisionRequest;
import ditda.backend.domain.commission.revision.exception.RevisionErrorCode;
import ditda.backend.domain.commission.revision.mapper.RevisionMapper;
import ditda.backend.domain.commission.revision.service.DesignerRevisionService;
import ditda.backend.domain.commission.revision.service.RevisionQueryService;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DesignerRevisionFacade {

	private static final ZoneId ZONE_KST = ZoneId.of("Asia/Seoul");

	private final DesignerCommissionService designerCommissionService;
	private final DraftQueryService draftQueryService;
	private final RevisionQueryService revisionQueryService;
	private final DesignerRevisionService designerRevisionService;
	private final RevisionMapper revisionMapper;
	private final CommissionService commissionService;
	private final ApplicationService applicationService;
	private final DesignerDraftService designerDraftService;
	private final DesignerDraftFileService designerDraftFileService;
	private final ApplicationEventPublisher eventPublisher;

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

	// 수정본 제출
	@Transactional
	public RevisionSubmitResponse submitRevision(
		Long designerId,
		Long commissionId,
		RevisionSubmitRequest request
	) {

		// 외주 조회 + 검증
		Commission commission = commissionService.getById(commissionId);
		commission.validateRevisable();

		// 최종 선택된 디자이너인지 검증
		CommissionApplication application =
			applicationService.getApplicationByCommissionAndDesigner(commissionId, designerId);
		application.validateRevisionSubmittable();

		// 현재 수정 요청 조회
		CommissionDraft latestDraft = draftQueryService.getLatestDraftOfSelectedApplication(commissionId);
		RevisionRequest revisionRequest = revisionQueryService.getRevisionRequestOnDraft(latestDraft.getId());

		// 이미 수정본 제출했는지 검증
		if (revisionQueryService.hasRevisionResponse(revisionRequest.getId())) {
			throw new GeneralException(RevisionErrorCode.REVISION_ALREADY_SUBMITTED);
		}

		// 파일 검증 + promote
		List<String> keys = request.keys();
		designerDraftFileService.validateFiles(keys);
		List<String> permanentKeys = designerDraftFileService.promote(keys);

		// 수정본 시안 + 파일 + 수정 답변 저장
		CommissionDraft newDraft;
		try {
			newDraft = designerDraftService.submitRevisionDraft(
				application,
				latestDraft.nextRound(),
				permanentKeys
			);
			designerRevisionService.createRevisionResponse(
				revisionRequest,
				newDraft,
				request.designerComment()
			);
		} catch (Exception original) {
			try {
				designerDraftFileService.deleteFiles(permanentKeys);
			} catch (Exception cleanupEx) {
				log.warn("수정본 저장 실패 후 S3 파일 정리 실패, keys={}", permanentKeys, cleanupEx);
				original.addSuppressed(cleanupEx);
			}
			throw original;
		}

		int currentRevisionCount = revisionQueryService.calculateCurrentRevisionCount(commission);

		// 강사에게 수정본 제출 이메일 발송
		publishRevisionSubmittedEvent(commissionId, currentRevisionCount);

		return revisionMapper.toRevisionSubmitResponse(
			newDraft,
			currentRevisionCount,
			commission
		);
	}

	// 수정본 제출 알림 이벤트 발행
	private void publishRevisionSubmittedEvent(Long commissionId, int currentRevisionCount) {

		Commission commission = commissionService.getWithInstructorAndUserById(commissionId);

		eventPublisher.publishEvent(new RevisionSubmittedEvent(
			commission.getId(),
			commission.getTitle(),
			commission.getInstructor().getUser().getEmail(),
			commission.getInstructor().getName(),
			currentRevisionCount,
			LocalDateTime.now(ZONE_KST)
		));
	}
}
