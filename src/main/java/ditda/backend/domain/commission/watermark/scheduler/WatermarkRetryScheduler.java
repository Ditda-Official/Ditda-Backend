package ditda.backend.domain.commission.watermark.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.watermark.service.WatermarkRetryService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WatermarkRetryScheduler {

	private final WatermarkRetryService watermarkRetryService;

	// 10분마다 미완료 워터마크 재처리
	@Scheduled(cron = "0 */10 * * * *", zone = "Asia/Seoul")
	public void retryIncompleteWatermarks() {
		watermarkRetryService.retryIncompleteFiles();
	}
}
