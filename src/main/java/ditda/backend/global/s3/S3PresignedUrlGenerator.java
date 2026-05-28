package ditda.backend.global.s3;

import java.time.Duration;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Component
@RequiredArgsConstructor
public class S3PresignedUrlGenerator {

	private final S3Presigner s3Presigner;
	private final S3Properties s3Properties;

	public String generatePrivateGetUrl(String key) {
		Duration ttl = Duration.ofMinutes(s3Properties.getPresignedUrlTtlMinutes());
		return generatePrivateGetUrl(key, ttl);
	}

	public String generatePrivateGetUrl(String key, Duration ttl) {
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
			.bucket(s3Properties.getPrivateBucket())
			.key(key)
			.build();

		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
			.signatureDuration(ttl)
			.getObjectRequest(getObjectRequest)
			.build();

		PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
		return presignedRequest.url().toString();
	}
}
