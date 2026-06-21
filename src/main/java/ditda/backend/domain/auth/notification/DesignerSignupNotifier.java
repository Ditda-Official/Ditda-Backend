package ditda.backend.domain.auth.notification;

import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import ditda.backend.domain.auth.event.DesignerSignedUpEvent;
import ditda.backend.global.config.AdminProperties;
import ditda.backend.global.s3.manager.S3PresignedUrlGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DesignerSignupNotifier {

	private static final Duration ADMIN_DOWNLOAD_URL_TTL = Duration.ofHours(6);        // URL 최대 유효기간 6시간

	private final DesignerSignupMailer designerSignupMailer;
	private final AdminProperties adminProperties;
	private final S3PresignedUrlGenerator presignedUrlGenerator;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onDesignerSignedUp(DesignerSignedUpEvent event) {

		// 포트폴리오가 없으면 메일 전송 X
		if (event.portfolioKeys().isEmpty()) {
			return;
		}

		try {
			List<String> portfolioUrls = event.portfolioKeys().stream()
				.map(key -> presignedUrlGenerator.generatePrivateGetUrl(key, ADMIN_DOWNLOAD_URL_TTL))
				.toList();

			designerSignupMailer.sendAdminNotification(
				adminProperties.getNotificationEmail(),
				event.userId(),
				event.name(),
				event.email(),
				portfolioUrls
			);
		} catch (Exception e) {
			log.error("Failed to dispatch designer signup notification. userId={}", event.userId(), e);
			// TODO : 디스코드 웹훅
		}
	}
}
