package ditda.backend.domain.auth.notification;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ditda.backend.domain.auth.event.DesignerSignedUpEvent;
import ditda.backend.global.config.AdminProperties;
import ditda.backend.global.email.NotificationOutbox;
import ditda.backend.global.email.NotificationOutboxRepository;
import ditda.backend.global.email.NotificationType;
import ditda.backend.global.s3.manager.S3PresignedUrlGenerator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DesignerSignupNotifier {

	private static final Duration ADMIN_DOWNLOAD_URL_TTL = Duration.ofHours(6);        // URL 최대 유효기간 6시간

	private final NotificationOutboxRepository outboxRepository;
	private final AdminProperties adminProperties;
	private final S3PresignedUrlGenerator presignedUrlGenerator;

	@EventListener
	public void onDesignerSignedUp(DesignerSignedUpEvent event) {

		// 포트폴리오가 없으면 메일 전송 X
		if (event.portfolioKeys().isEmpty()) {
			return;
		}

		// 메일 전송 시간
		LocalDateTime mailScheduledAt = event.mailScheduledAt();

		List<String> portfolioUrls = event.portfolioKeys().stream()
			.map(key -> presignedUrlGenerator.generatePrivateGetUrl(key, ADMIN_DOWNLOAD_URL_TTL))
			.toList();

		registerAdminSignupReview(event, portfolioUrls, mailScheduledAt);

	}

	// 어드민 디자이너 회원가입 확인 메일 발송
	private void registerAdminSignupReview(DesignerSignedUpEvent event, List<String> portfolioUrls,
		LocalDateTime mailScheduledAt) {
		outboxRepository.save(NotificationOutbox.create(
			adminProperties.getNotificationEmail(),
			NotificationType.DESIGNER_SIGNUP_REVIEW_ADMIN,
			Map.of(
				"userId", event.userId(),
				"name", event.name(),
				"email", event.email(),
				"portfolioUrls", portfolioUrls
			),
			mailScheduledAt
		));
	}
}
