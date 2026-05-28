package ditda.backend.global.s3;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;

@Component
@RequiredArgsConstructor
public class S3UrlResolver {

	private final S3Client s3Client;
	private final S3Properties s3Properties;

	public String toPublicS3Url(String key) {
		if (key == null || key.isEmpty()) {
			return key;
		}

		return s3Client.utilities()
			.getUrl(builder -> builder
				.bucket(s3Properties.getPublicBucket())
				.key(key)
			)
			.toString();
	}
}
