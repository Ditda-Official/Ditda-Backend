package ditda.backend.domain.commission.draft.service;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.CommissionApplication;
import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import ditda.backend.domain.commission.application.service.ApplicationService;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.draft.entity.CommissionDraft;
import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.domain.commission.draft.repository.CommissionDraftFileRepository;
import ditda.backend.domain.commission.draft.repository.CommissionDraftRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DesignerDraftService {

	private final ApplicationService applicationService;
	private final CommissionDraftRepository commissionDraftRepository;
	private final CommissionDraftFileRepository commissionDraftFileRepository;

	// 디자이너 지원 조회 + 지원 상태 검증
	public CommissionApplication findApplicationReadyForDraftSubmission(Long commissionId, Long designerId) {

		// 디자이너 조회
		CommissionApplication application = applicationService
			.getApplicationByCommissionAndDesigner(commissionId, designerId);

		// 지원 상태 검증
		application.validateDraftSubmittable();

		return application;
	}

	// 시안 저장 + 지원 상태 전이 + 외주 상태 전이
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
			commission.startDraftSelecting();
			// TODO: 이메일 발송
		}

		return draft;
	}
}
