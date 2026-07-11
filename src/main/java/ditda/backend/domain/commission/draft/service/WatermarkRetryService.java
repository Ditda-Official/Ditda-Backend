package ditda.backend.domain.commission.draft.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
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
	private static final int BATCH_SIZE = 20;

	private final CommissionDraftFileRepository commissionDraftFileRepository;
	private final DraftWatermarkService draftWatermarkService;

	// 미완료 워터마크 파일 조회 후 워터마크 큐에 투입
	@Transactional
	public void retryIncompleteFiles() {

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime stuckBefore = now.minus(STUCK_THRESHOLD);

		List<Long> targetIds = commissionDraftFileRepository.findWatermarkRetryTargetIds(
			WatermarkStatus.FAILED,
			WatermarkStatus.PROCESSING,
			CommissionDraftFile.MAX_WATERMARK_RETRY,
			stuckBefore,
			PageRequest.of(0, BATCH_SIZE)
		);

		if (targetIds.isEmpty()) {
			return;
		}

		int claimed = commissionDraftFileRepository.claimForRetry(
			targetIds,
			WatermarkStatus.PROCESSING,
			WatermarkStatus.COMPLETED,
			CommissionDraftFile.MAX_WATERMARK_RETRY,
			now
		);

		targetIds.forEach(draftWatermarkService::reprocessFile);

		log.info("워터마크 재처리 대상 선점 {}건 / 큐잉 {}건", claimed, targetIds.size());
	}
}
