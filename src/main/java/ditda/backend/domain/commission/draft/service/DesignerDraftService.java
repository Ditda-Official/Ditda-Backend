package ditda.backend.domain.commission.draft.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.CommissionApplication;
import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import ditda.backend.domain.commission.application.service.ApplicationService;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.event.AllFirstDraftsSubmittedEvent;
import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.domain.commission.draft.repository.CommissionDraftFileRepository;
import ditda.backend.domain.commission.draft.repository.CommissionDraftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DesignerDraftService {

	private static final ZoneId ZONE_KST = ZoneId.of("Asia/Seoul");

	private final ApplicationService applicationService;
	private final CommissionDraftRepository commissionDraftRepository;
	private final CommissionDraftFileRepository commissionDraftFileRepository;
	private final ApplicationEventPublisher eventPublisher;

	// 디자이너 지원 조회 + 지원 상태 검증
	public CommissionApplication findApplicationReadyForDraftSubmission(Long commissionId, Long designerId) {

		// 디자이너 조회
		CommissionApplication application = applicationService
			.getApplicationByCommissionAndDesigner(commissionId, designerId);

		// 지원 상태 검증
		application.validateDraftSubmittable();

		return application;
	}

	// 시안 저장 + 지원 상태 전이 + 외주 상태 전이 및 이벤트 발행
	@Transactional
	public CommissionDraft submitDraft(
		Commission commission,
		CommissionApplication application,
		List<String> keys
	) {

		// 외주 상태 검증
		commission.validateDraftSubmittable();

		// 시안 저장
		CommissionDraft draft = commissionDraftRepository.save(CommissionDraft.createFirstRound(application));

		// 시안 파일 저장
		List<CommissionDraftFile> draftFiles = IntStream.range(0, keys.size())
			.mapToObj(i -> CommissionDraftFile.create(draft, i, keys.get(i)))
			.toList();
		commissionDraftFileRepository.saveAll(draftFiles);

		// 지원 상태 전이 (ASSIGNED -> DRAFT_SUBMITTED)
		application.markDraftSubmitted();

		// 모든 지원자가 시안 제출을 완료했으면 외주 상태 전이 (DRAFT_SUBMITTING -> DRAFT_SELECTING)
		long remainingAssigned = applicationService.countByCommissionAndStatus(
			commission.getId(),
			ApplicationStatus.ASSIGNED
		);
		if (remainingAssigned == 0) {
			// 제출된 시안 수
			long submittedCount = applicationService.countByCommissionAndStatus(
				commission.getId(),
				ApplicationStatus.DRAFT_SUBMITTED
			);

			commission.startDraftSelecting();
			publishAllFirstDraftsSubmittedEvent(commission, (int)submittedCount);

			log.info("모든 1차 시안 제출 완료. commissionId={}, submittedCount={}",
				commission.getId(), submittedCount);
		}

		return draft;
	}

	// 모든 1차 시안 제출 완료 이벤트 발행
	private void publishAllFirstDraftsSubmittedEvent(Commission commission, int submittedCount) {

		eventPublisher.publishEvent(new AllFirstDraftsSubmittedEvent(
			commission.getId(),
			commission.getTitle(),
			commission.getInstructor().getUser().getEmail(),
			commission.getInstructor().getName(),
			submittedCount,
			LocalDateTime.now(ZONE_KST)
		));
	}
}
