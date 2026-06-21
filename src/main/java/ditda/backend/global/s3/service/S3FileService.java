package ditda.backend.global.s3.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.s3.config.S3Properties;
import ditda.backend.global.s3.dto.PresignedUpload;
import ditda.backend.global.s3.enums.S3ContentType;
import ditda.backend.global.s3.enums.UploadTarget;
import ditda.backend.global.s3.exception.S3ErrorCode;
import ditda.backend.global.s3.manager.S3UploadManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class S3FileService {

	private final S3UploadManager s3UploadManager;
	private final S3Properties s3Properties;

	public PresignedUpload issuePresignedUpload(UploadTarget target, String contentType) {

		// 파일 형식 검증
		S3ContentType type = S3ContentType.from(contentType);
		if (type == null || !target.getAllowed().contains(type)) {
			throw new GeneralException(S3ErrorCode.UNSUPPORTED_CONTENT_TYPE);
		}

		// temp key 및 presigned URL 발급
		return s3UploadManager.issueTempUpload(
			target.getBucketType(),
			target.getDir(),
			type.getExtension(),
			contentType
		);
	}

	// temp key 및 파일 사이즈 검증
	public void validateUploadedKeys(UploadTarget target, List<String> keys) {

		// 빈 키 여부 검증
		if (keys == null) {
			throw new GeneralException(S3ErrorCode.INVALID_FILE);
		}

		// 중복 키 검증
		if (keys.size() != keys.stream().distinct().count()) {
			throw new GeneralException(S3ErrorCode.INVALID_FILE);
		}

		long maxBytes = s3Properties.getMaxFileSize().toBytes();
		for (String key : keys) {
			// temp key 형식 검증
			if (!s3UploadManager.isTempKey(key, target.getDir())) {
				throw new GeneralException(S3ErrorCode.INVALID_FILE);
			}

			// 파일 크기 검증
			Long size = s3UploadManager.getObjectSize(target.getBucketType(), key);
			if (size == null) {
				throw new GeneralException(S3ErrorCode.INVALID_FILE);
			}
			if (size > maxBytes) {
				throw new GeneralException(S3ErrorCode.FILE_SIZE_EXCEEDED);
			}
		}
	}
}
