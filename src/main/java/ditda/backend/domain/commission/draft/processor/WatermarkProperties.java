package ditda.backend.domain.commission.draft.processor;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "watermark")
public record WatermarkProperties(
	String mode,
	Lambda lambda
) {

	public record Lambda(
		String functionName,
		String callbackUrl
	) {
	}
}
