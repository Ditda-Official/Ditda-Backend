package ditda.backend.domain.commission.draft.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.domain.commission.draft.entity.enums.WatermarkStatus;
import ditda.backend.domain.commission.draft.repository.CommissionDraftFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatermarkRetryService {

	private static final Duration STUCK_THRESHOLD = Duration.ofMinutes(30);

	private final CommissionDraftFileRepository commissionDraftFileRepository;
	private final DraftWatermarkService draftWatermarkService;

	// 미완료 워터마크 파일 조회 후 워터마크 큐에 투입
	@Transactional(readOnly = true)
	public void retryIncompleteFiles() {

		LocalDateTime stuckBefore = LocalDateTime.now().minus(STUCK_THRESHOLD);

		List<CommissionDraftFile> targets = commissionDraftFileRepository.findWatermarkRetryTargets(
			WatermarkStatus.FAILED,
			WatermarkStatus.PROCESSING,
			CommissionDraftFile.MAX_WATERMARK_RETRY,
			stuckBefore
		);

		if (targets.isEmpty()) {
			return;
		}

		for (CommissionDraftFile target : targets) {
			draftWatermarkService.reprocessFile(target.getId());
		}

		log.info("워터마크 재처리 대상 {}건 큐잉 완료", targets.size());
	}
}
