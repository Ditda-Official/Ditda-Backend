package ditda.backend.domain.commission.core.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.service.CommissionDeadlineService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommissionScheduler {

	private final CommissionDeadlineService commissionDeadlineService;

	@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
	public void processApplicationDeadlines() {
		commissionDeadlineService.processApplicationDeadlines();
	}

	@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
	public void processFirstDraftDeadlines() {
		commissionDeadlineService.processFirstDraftDeadlines();
	}

	@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
	public void processFinalDeadlines() {
		commissionDeadlineService.processFinalDeadlines();
	}
}
