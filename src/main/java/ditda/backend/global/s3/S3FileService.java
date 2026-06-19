package ditda.backend.global.s3;

import org.springframework.stereotype.Service;

import ditda.backend.global.apipayload.code.GeneralErrorCode;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.s3.enums.S3ContentType;
import ditda.backend.global.s3.enums.UploadTarget;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class S3FileService {

	private final S3UploadManager s3UploadManager;

	public PresignedUpload issuePresignedUpload(UploadTarget target, String contentType) {

		S3ContentType type = S3ContentType.from(contentType);
		if (type == null || !target.getAllowed().contains(type)) {
			throw new GeneralException(GeneralErrorCode.UNSUPPORTED_CONTENT_TYPE);
		}

		return s3UploadManager.issueTempUpload(
			target.getBucketType(),
			target.getDir(),
			type.getExtension(),
			contentType
		);
	}
}
