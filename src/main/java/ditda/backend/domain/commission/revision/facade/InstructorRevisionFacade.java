package ditda.backend.domain.commission.revision.facade;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.event.RevisionRequestedEvent;
import ditda.backend.domain.commission.core.service.InstructorCommissionService;
import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.domain.commission.draft.service.DraftQueryService;
import ditda.backend.domain.commission.revision.dto.request.RevisionCreateRequest;
import ditda.backend.domain.commission.revision.dto.response.InstructorRevisionDetailResponse;
import ditda.backend.domain.commission.revision.exception.RevisionErrorCode;
import ditda.backend.domain.commission.revision.mapper.RevisionMapper;
import ditda.backend.domain.commission.revision.service.InstructorRevisionService;
import ditda.backend.domain.commission.revision.service.RevisionQueryService;
import ditda.backend.domain.user.entity.User;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorRevisionFacade {

	private final InstructorCommissionService instructorCommissionService;
	private final RevisionQueryService revisionQueryService;
	private final InstructorRevisionService instructorRevisionService;
	private final DraftQueryService draftQueryService;
	private final RevisionMapper revisionMapper;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public InstructorRevisionDetailResponse getRevisionDetail(Long instructorId, Long commissionId) {

		// 외주 조회 + 강사 확인
		Commission commission = instructorCommissionService.getOwnedCommission(commissionId, instructorId);

		// 수정 단계인지 검증
		commission.validateRevisable();

		// 선택된 디자이너의 가장 최근 시안
		CommissionDraft latestDraft = draftQueryService.getLatestDraftOfSelectedApplication(commissionId);

		// 시안에 달린 디자이너 코멘트 조회 + 확인 처리
		String designerComment = instructorRevisionService.getDesignerCommentAndCheck(latestDraft.getId());

		// 썸네일
		CommissionDraftFile thumbnail = draftQueryService.findThumbnail(latestDraft.getId());

		// 현재 수정 차수
		int currentRevisionCount = revisionQueryService.calculateCurrentRevisionCount(commission);

		return revisionMapper.toInstructorRevisionDetailResponse(
			commission,
			latestDraft,
			thumbnail,
			designerComment,
			currentRevisionCount
		);
	}

	@Transactional
	public void createRevision(Long instructorId, Long commissionId, RevisionCreateRequest request) {

		// 외주 조회 + 강사 확인
		Commission commission =
			instructorCommissionService.getOwnedCommissionWithInstructorAndAssignedDesigner(commissionId, instructorId);

		// 수정 단계인지 검증
		commission.validateRevisable();

		// 카테고리 중복 검증
		validateDistinctCategories(request);

		// 수정 횟수 한도 검증
		int current = revisionQueryService.calculateCurrentRevisionCount(commission);
		if (commission.isRevisionLimitExceeded(current)) {
			throw new GeneralException(RevisionErrorCode.REVISION_LIMIT_EXCEEDED);
		}

		// 선택된 디자이너의 가장 최근 시안
		CommissionDraft latestDraft = draftQueryService.getLatestDraftOfSelectedApplication(commissionId);

		// 시안에 이미 수정 요청이 존재하는지 검증
		if (revisionQueryService.hasRevisionRequestOnDraft(latestDraft.getId())) {
			throw new GeneralException(RevisionErrorCode.REVISION_ALREADY_REQUESTED);
		}

		// 수정 요청 저장
		instructorRevisionService.createRevisionRequest(commission, latestDraft, request);

		// 생성된 수정 요청을 반영한 수정 횟수
		int currentRevisionCount = current + 1;

		// 디자이너에게 수정 요청 도착 메일 발송
		publishRevisionRequestedEvent(commission, currentRevisionCount);
	}

	// 카테고리 중복 검증
	private void validateDistinctCategories(RevisionCreateRequest request) {

		long distinctCount = request.categories().stream()
			.map(RevisionCreateRequest.RevisionCreateCategory::category)
			.distinct()
			.count();

		if (distinctCount != request.categories().size()) {
			throw new GeneralException(RevisionErrorCode.DUPLICATE_REVISION_CATEGORY);
		}
	}

	// 수정 요청 알림 이벤트 발행
	private void publishRevisionRequestedEvent(Commission commission, int currentRevisionCount) {

		User designerUser = commission.getAssignedDesigner().getUser();

		eventPublisher.publishEvent(new RevisionRequestedEvent(
			commission.getId(),
			commission.getTitle(),
			designerUser.getEmail(),
			designerUser.getName(),
			currentRevisionCount,
			LocalDateTime.now()
		));
	}
}
