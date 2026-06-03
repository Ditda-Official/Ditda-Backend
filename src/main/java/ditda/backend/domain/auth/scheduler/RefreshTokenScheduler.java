package ditda.backend.domain.auth.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ditda.backend.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenScheduler {

	private final AuthService authService;

	// 새벽 3시 기준 만료된 Refresh 토큰 전체 삭제
	@Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
	public void deleteExpiredRefreshTokens() {

		int deletedCount = authService.deleteExpiredRefreshTokens();

		log.info("만료된 Refresh 토큰 삭제 완료. count={}", deletedCount);
	}
}
