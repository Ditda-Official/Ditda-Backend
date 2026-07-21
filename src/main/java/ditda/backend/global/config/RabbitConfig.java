package ditda.backend.global.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateCustomizer;
import org.springframework.boot.amqp.autoconfigure.SimpleRabbitListenerContainerFactoryConfigurer;
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
					log.error("RabbitMQ publish nack. correlationData={}, cause={}", correlationData, cause);
				}
			});

			template.setReturnsCallback(returned -> {
				log.error("RabbitMQ unroutable message. replyText={}, exchange={}, routingKey={}",
					returned.getReplyText(), returned.getExchange(), returned.getRoutingKey());
			});
		};
	}

	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
		SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {

		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		configurer.configure(factory, connectionFactory);

		// RabbitMQ 메시지 헤더를 통해 traceId와 spanId를 전파
		factory.setObservationEnabled(true);

		return factory;
	}

	@Bean
	public MessageConverter messageConverter() {
		return new JacksonJsonMessageConverter();
	}
}
