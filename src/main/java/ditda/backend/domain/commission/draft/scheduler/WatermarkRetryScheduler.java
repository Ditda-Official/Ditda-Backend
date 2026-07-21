package ditda.backend.domain.commission.draft.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.draft.service.WatermarkRetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WatermarkRetryScheduler {

	private final WatermarkRetryService watermarkRetryService;

	// 10분마다 미완료 워터마크 재처리
	@Scheduled(cron = "0 */10 * * * *", zone = "Asia/Seoul")
	public void retryIncompleteWatermarks() {

		log.info("워터마크 재처리 스케줄 시작");
		watermarkRetryService.retryIncompleteFiles();
		log.info("워터마크 재처리 스케줄 완료");
	}
}
