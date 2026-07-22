package ditda.backend.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.services.lambda.LambdaClient;

@Configuration
@ConditionalOnProperty(name = "watermark.mode", havingValue = "lambda")
public class LambdaConfig {

	@Bean
	public LambdaClient lambdaClient() {
		return LambdaClient.create();
	}
}
