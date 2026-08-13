package ditda.backend.global.config;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RabbitConfig {

	@Bean
	public RabbitTemplateCustomizer rabbitTemplateCustomizer() {
		return template -> {
			template.setObservationEnabled(true);

			template.setConfirmCallback((correlationData, ack, cause) -> {
				if (!ack) {
					log.error("RabbitMQ publish nack. correlationId={}, cause={}",
						correlationData != null ? correlationData.getId() : null, cause);
				}
			});

			template.setReturnsCallback(returned -> {
				log.error("RabbitMQ unroutable message. replyText={}, exchange={}, routingKey={}",
					returned.getReplyText(), returned.getExchange(), returned.getRoutingKey());
			});
		};
	}

	@Bean
	public MessageConverter messageConverter() {
		return new JacksonJsonMessageConverter();
	}
}
