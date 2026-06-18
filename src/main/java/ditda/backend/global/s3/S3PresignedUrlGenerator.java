package ditda.backend.global.s3;

import java.time.Duration;

import org.springframework.stereotype.Component;

import ditda.backend.global.apipayload.code.GeneralErrorCode;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.s3.enums.BucketType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3PresignedUrlGenerator {

	private final S3Presigner s3Presigner;
	private final S3Properties s3Properties;

	public String generatePrivateGetUrl(String key) {
		Duration ttl = Duration.ofMinutes(s3Properties.getPresignedUrlTtlMinutes());
		return generatePrivateGetUrl(key, ttl);
	}

	public String generatePrivateGetUrl(String key, Duration ttl) {
		try {
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
		} catch (SdkException exception) {
			log.error("Failed to generate private presigned url. key={}", key, exception);
			throw new GeneralException(GeneralErrorCode.FILE_URL_GENERATION_FAILED);
		}
	}

	public String generatePutUrl(BucketType bucketType, String key, String contentType) {
		Duration ttl = Duration.ofMinutes(s3Properties.getPresignedUrlTtlMinutes());
		return generatePutUrl(bucketType, key, contentType, ttl);
	}

	public String generatePutUrl(BucketType bucketType, String key, String contentType, Duration ttl) {
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(s3Properties.getBucket(bucketType))
			.key(key)
			.contentType(contentType)
			.build();

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
			.signatureDuration(ttl)
			.putObjectRequest(putObjectRequest)
			.build();

		return s3Presigner.presignPutObject(presignRequest).url().toString();
	}
}
