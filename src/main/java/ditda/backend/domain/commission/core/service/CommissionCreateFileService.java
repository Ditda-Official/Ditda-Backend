package ditda.backend.domain.commission.core.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest.FileInfo;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.CommissionFile;
import ditda.backend.domain.commission.core.entity.enums.FileKind;
import ditda.backend.domain.commission.core.exception.CommissionErrorCode;
import ditda.backend.domain.commission.core.repository.CommissionFileRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.s3.S3FileService;
import ditda.backend.global.s3.S3UploadManager;
import ditda.backend.global.s3.enums.BucketType;
import ditda.backend.global.s3.enums.UploadTarget;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommissionCreateFileService {

	private static final BucketType BUCKET = BucketType.PRIVATE;

	private final CommissionFileRepository commissionFileRepository;
	private final S3UploadManager s3UploadManager;
	private final S3FileService s3FileService;

	// 첨부 파일의 종류별로 keys 합산하여 검증
	// 클라이언트가 같은 종류 여러번 보내도 종류별 총량을 기준으로 검사
	public void validateFiles(List<FileInfo> files) {

		Map<FileKind, List<String>> keysByKind = files.stream()
			.collect(Collectors.groupingBy(
				FileInfo::fileKind,
				Collectors.flatMapping(file -> file.keys().stream(), Collectors.toList())
			));

		keysByKind.forEach(this::validateKeys);
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

		// 파일 DB에 일괄 저장
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

	private static UploadTarget targetOf(FileKind fileKind) {
		return switch (fileKind) {
			case MATERIAL -> UploadTarget.COMMISSION_MATERIAL;
			case REFERENCE -> UploadTarget.COMMISSION_REFERENCE;
		};
	}

	private void validateKeys(FileKind fileKind, List<String> keys) {

		// 파일 개수 검증
		if (keys.size() > fileKind.getMaxCount()) {
			throw new GeneralException(
				CommissionErrorCode.COMMISSION_FILE_LIMIT_EXCEEDED,
				"%s 파일은 최대 %d개까지 업로드 가능합니다.".formatted(fileKind, fileKind.getMaxCount())
			);
		}

		s3FileService.validateUploadedKeys(targetOf(fileKind), keys);
	}
}
