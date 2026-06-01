package ditda.backend.global.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import ditda.backend.global.s3.enums.BucketType;
import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "app.s3")
public class S3Properties {

	private DataSize maxFileSize = DataSize.ofMegabytes(30);

	private String publicBucket;

	private String privateBucket;

	private int presignedUrlTtlMinutes = 10;

	public String getBucket(BucketType bucketType) {
		return switch (bucketType) {
			case PUBLIC -> publicBucket;
			case PRIVATE -> privateBucket;
		};
	}
}
