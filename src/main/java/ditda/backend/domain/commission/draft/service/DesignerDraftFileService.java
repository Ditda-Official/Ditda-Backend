package ditda.backend.domain.commission.draft.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ditda.backend.global.s3.enums.BucketType;
import ditda.backend.global.s3.enums.UploadTarget;
import ditda.backend.global.s3.manager.S3UploadManager;
import ditda.backend.global.s3.service.S3FileService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DesignerDraftFileService {

	private static final BucketType BUCKET = BucketType.PRIVATE;

	private final S3FileService s3FileService;
	private final S3UploadManager s3UploadManager;

	// 파일 검증
	public void validateFiles(List<String> keys) {
		s3FileService.validateUploadedKeys(UploadTarget.COMMISSION_DRAFT, keys);
	}

	public List<String> promote(List<String> tempKeys) {
		return s3UploadManager.promote(BUCKET, tempKeys);
	}

	public void deleteFiles(List<String> keys) {
		s3UploadManager.deleteAll(BUCKET, keys);
	}
}
