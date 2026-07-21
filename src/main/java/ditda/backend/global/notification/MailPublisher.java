package ditda.backend.global.notification;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MailPublisher {

	private final RabbitTemplate rabbitTemplate;

	public void publish(MailMessage message) {
		rabbitTemplate.convertAndSend(MailRabbitConfig.MAIL_EXCHANGE, MailRabbitConfig.MAIL_ROUTING_KEY, message);
	}

	public void publish(MailMessage message, CorrelationData correlationData) {
		rabbitTemplate.convertAndSend(
			MailRabbitConfig.MAIL_EXCHANGE,
			MailRabbitConfig.MAIL_ROUTING_KEY,
			message,
			correlationData
		);
	}
}
