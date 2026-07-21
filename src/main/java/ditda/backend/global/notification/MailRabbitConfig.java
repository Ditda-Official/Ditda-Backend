package ditda.backend.global.notification;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailRabbitConfig {

	public static final String MAIL_EXCHANGE = "mail.exchange";
	public static final String MAIL_ROUTING_KEY = "mail.send";

	@Bean
	DirectExchange mailExchange() {
		return new DirectExchange(MAIL_EXCHANGE);
	}
}
