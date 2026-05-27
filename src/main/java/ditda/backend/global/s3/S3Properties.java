package ditda.backend.global.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "app.s3")
public class S3Properties {

	private String publicBucket;

	private String privateBucket;

	private int presignedUrlTtlMinutes = 10;
}
