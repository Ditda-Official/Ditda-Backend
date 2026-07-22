package ditda.backend.domain.commission.watermark.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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
	private final DraftWatermarkTransitionService draftWatermarkTransitionService;

	// 미완료 워터마크 파일 조회 후 워터마크 큐에 투입
	public void retryIncompleteFiles() {

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime stuckBefore = now.minus(STUCK_THRESHOLD);

		// 1. 재시도 상한을 넘겨 PROCESSING에 정체된 파일은 FAILED로 전이
		int exhausted = draftWatermarkTransitionService.failExhaustedStuckFiles(stuckBefore, now);
		if (exhausted > 0) {
			// TODO: 디스코드 웹훅
			log.warn("재시도 상한 초과 정체 파일 FAILED 전환 {}건", exhausted);
		}

		// 2. 재처리 대상 조회
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

		// 3. 선점에 성공된 파일만 큐잉
		List<Long> claimedIds = targetIds.stream()
			.filter(id -> draftWatermarkTransitionService.claimForRetry(id, stuckBefore, now))
			.toList();

		claimedIds.forEach(draftWatermarkService::reprocessFile);

		log.info("워터마크 재처리 대상 {}건 / 큐잉 {}건", targetIds.size(), claimedIds.size());
	}
}
