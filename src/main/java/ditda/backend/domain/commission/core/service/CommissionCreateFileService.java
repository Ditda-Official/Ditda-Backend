package ditda.backend.domain.commission.core.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.dto.response.CommissionFilePresignResponse;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.CommissionFile;
import ditda.backend.domain.commission.core.entity.enums.FileKind;
import ditda.backend.domain.commission.core.exception.CommissionErrorCode;
import ditda.backend.domain.commission.core.repository.CommissionFileRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.s3.PresignedUpload;
import ditda.backend.global.s3.S3Properties;
import ditda.backend.global.s3.S3UploadManager;
import ditda.backend.global.s3.enums.BucketType;
import ditda.backend.global.s3.enums.S3ContentType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommissionCreateFileService {

	private static final String DIR = "commission";
	private static final BucketType BUCKET = BucketType.PRIVATE;
	private static final Set<S3ContentType> ALLOWED = EnumSet.of(S3ContentType.PNG);

	private final S3UploadManager s3UploadManager;
	private final CommissionFileRepository commissionFileRepository;
	private final S3Properties s3Properties;

	public CommissionFilePresignResponse generatePresignedUpload(FileKind fileKind, String contentType) {

		S3ContentType type = S3ContentType.from(contentType);
		if (type == null || !ALLOWED.contains(type)) {
			throw new GeneralException(CommissionErrorCode.INVALID_COMMISSION_FILE);
		}

		PresignedUpload upload = s3UploadManager.issueTempUpload(BUCKET, directoryOf(fileKind), type.getExtension(), contentType);

		return new CommissionFilePresignResponse(upload.key(), upload.presignedUrl());
	}

	public void validateKeys(FileKind fileKind, List<String> keys) {

		if (keys.size() > fileKind.getMaxCount()) {
			throw new GeneralException(
				CommissionErrorCode.COMMISSION_FILE_LIMIT_EXCEEDED,
				"%s 파일은 최대 %d개까지 업로드 가능합니다.".formatted(fileKind, fileKind.getMaxCount())
			);
		}

		if (keys.size() != keys.stream().distinct().count()) {
			throw new GeneralException(CommissionErrorCode.INVALID_COMMISSION_FILE);
		}

		String dir = directoryOf(fileKind);
		for (String key : keys) {
			if (!s3UploadManager.isTempKey(key, dir)) {
				throw new GeneralException(CommissionErrorCode.INVALID_COMMISSION_FILE);
			}

			Long size = s3UploadManager.getObjectSize(BUCKET, key);
			if (size == null) {
				throw new GeneralException(CommissionErrorCode.INVALID_COMMISSION_FILE);
			}
			if (size > s3Properties.getMaxFileSize().toBytes()) {
				throw new GeneralException(CommissionErrorCode.COMMISSION_FILE_SIZE_EXCEEDED);
			}
		}
	}

	@Transactional
	public void saveCommissionFiles(
		Commission commission,
		FileKind fileKind,
		List<String> keys,
		String description
	) {

		if (keys == null || keys.isEmpty()) {
			return;
		}

		List<CommissionFile> commissionFiles = keys.stream()
			.map(key -> CommissionFile.create(commission, fileKind, key, description))
			.toList();
		commissionFileRepository.saveAll(commissionFiles);
	}

	public List<String> promote(List<String> tempKeys) {
		return s3UploadManager.promote(BUCKET, tempKeys);
	}

	public void deleteFiles(List<String> keys) {
		s3UploadManager.deleteAll(BUCKET, keys);
	}

	private String directoryOf(FileKind fileKind) {
		return DIR + "/" + fileKind.name().toLowerCase();
	}
}
