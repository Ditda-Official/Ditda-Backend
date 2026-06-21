package ditda.backend.global.s3.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import ditda.backend.global.s3.dto.PresignedUpload;
import ditda.backend.global.s3.enums.BucketType;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class S3UploadManager {

	private static final String TEMP_DIR = "tmp";

	private final S3PresignedUrlGenerator s3PresignedUrlGenerator;
	private final S3FileManager s3FileManager;

	public PresignedUpload issueTempUpload(BucketType bucketType, String dir, String extension, String contentType) {
		String key = "%s/%s/%s%s".formatted(dir, TEMP_DIR, UUID.randomUUID(), extension);
		String presignedUrl = s3PresignedUrlGenerator.generatePutUrl(bucketType, key, contentType);
		return new PresignedUpload(key, presignedUrl);
	}

	public boolean isTempKey(String key, String dir) {
		return key != null && key.startsWith(dir + "/" + TEMP_DIR + "/");
	}

	public Long getObjectSize(BucketType bucketType, String key) {
		return s3FileManager.getObjectSize(bucketType, key);
	}

	public List<String> promote(BucketType bucketType, List<String> tempKeys) {
		if (tempKeys.isEmpty()) {
			return List.of();
		}

		List<String> permanentKeys = new ArrayList<>();
		try {
			for (String tempKey : tempKeys) {
				String permanentKey = tempKey.replaceFirst("/" + TEMP_DIR + "/", "/");    // '/tmp/' -> '/'
				s3FileManager.copy(bucketType, tempKey, permanentKey);
				permanentKeys.add(permanentKey);
			}
		} catch (Exception e) {
			s3FileManager.deleteAll(bucketType, permanentKeys);   // 승격된 객체 되돌림
			throw e;
		}

		s3FileManager.deleteAll(bucketType, tempKeys);            // tmp 객체 정리
		return permanentKeys;
	}

	public void deleteAll(BucketType bucketType, List<String> keys) {
		s3FileManager.deleteAll(bucketType, keys);
	}
}
