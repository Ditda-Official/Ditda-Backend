package ditda.backend.domain.commission.draft.service;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.CommissionApplication;
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

	private final CommissionDraftRepository commissionDraftRepository;
	private final CommissionDraftFileRepository commissionDraftFileRepository;

	// 시안 저장 + 지원 상태 전이
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

		return draft;
	}

	// 다음 라운드 시안 및 파일 저장
	@Transactional
	public CommissionDraft submitRevisionDraft(
		CommissionApplication application,
		int round,
		List<String> keys
	) {

		CommissionDraft draft =
			commissionDraftRepository.save(CommissionDraft.create(application, round));

		List<CommissionDraftFile> draftFiles = IntStream.range(0, keys.size())
			.mapToObj(i -> CommissionDraftFile.create(draft, i, keys.get(i)))
			.toList();
		commissionDraftFileRepository.saveAll(draftFiles);

		return draft;
	}

}
