package ditda.backend.global.s3.manager;

import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Component;

import ditda.backend.global.s3.config.S3Properties;
import ditda.backend.global.s3.enums.BucketType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3FileManager {

	private final S3Client s3Client;
	private final S3Properties s3Properties;

	public Long getObjectSize(BucketType bucketType, String key) {
		String bucket = s3Properties.getBucket(bucketType);

		try {
			return s3Client.headObject(req -> req.bucket(bucket).key(key)).contentLength();
		} catch (S3Exception e) {
			if (e.statusCode() == 404) {    // 객체 없음 오류
				return null;
			}

			throw e;
		}
	}

	public void deleteAll(BucketType bucketType, List<String> keys) {
		if (keys == null || keys.isEmpty()) {
			return;
		}

		String bucket = s3Properties.getBucket(bucketType);
		for (String key : keys) {
			try {
				s3Client.deleteObject(req -> req.bucket(bucket).key(key));
			} catch (Exception e) {
				log.error("S3 delete failed. bucket={}, key={}", bucket, key, e);
			}
		}
	}

	public void copy(BucketType bucketType, String sourceKey, String destinationKey) {
		String bucket = s3Properties.getBucket(bucketType);

		s3Client.copyObject(req -> req
			.sourceBucket(bucket)
			.sourceKey(sourceKey)
			.destinationBucket(bucket)
			.destinationKey(destinationKey));
	}

	public InputStream download(BucketType bucketType, String key) {
		String bucket = s3Properties.getBucket(bucketType);
		return s3Client.getObject(req -> req.bucket(bucket).key(key));
	}

	public void upload(BucketType bucketType, String key, byte[] bytes, String contentType) {
		String bucket = s3Properties.getBucket(bucketType);
		s3Client.putObject(
			req -> req.bucket(bucket).key(key).contentType(contentType),
			RequestBody.fromBytes(bytes)
		);
	}
}
