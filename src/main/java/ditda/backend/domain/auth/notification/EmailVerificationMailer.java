package ditda.backend.domain.auth.notification;

import java.util.Map;

import org.springframework.amqp.AmqpException;
import org.springframework.stereotype.Component;

import ditda.backend.domain.auth.exception.AuthErrorCode;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.notification.MailMessage;
import ditda.backend.global.notification.MailPublisher;
import ditda.backend.global.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailVerificationMailer {

	private final MailPublisher mailPublisher;

	public void sendVerificationCode(String to, String code) {
		NotificationType type = NotificationType.EMAIL_VERIFICATION;
		try {
			mailPublisher.publish(new MailMessage(type.name(), to, Map.of("code", code)));
		} catch (AmqpException e) {
			log.error("인증 메일 발행 실패.", e);
			throw new GeneralException(AuthErrorCode.EMAIL_VERIFICATION_SEND_FAILED);
		}
	}
}
