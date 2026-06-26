package ditda.backend.domain.commission.core.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.service.CommissionDeadlineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommissionScheduler {

	private final CommissionDeadlineService commissionDeadlineService;

	@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
	public void processApplicationDeadlines() {
		log.info("외주 지원 마감 DB 상태 처리 시작");
		commissionDeadlineService.processApplicationDeadlines();
		log.info("외주 지원 마감 DB 상태 처리 완료");
	}

	@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
	public void processFirstDraftDeadlines() {
		log.info("외주 1차 시안 마감 DB 상태 처리 시작");
		commissionDeadlineService.processFirstDraftDeadlines();
		log.info("외주 1차 시안 마감 DB 상태 처리 완료");
	}

}
