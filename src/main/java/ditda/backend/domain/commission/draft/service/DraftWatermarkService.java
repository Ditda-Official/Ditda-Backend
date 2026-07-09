package ditda.backend.domain.commission.draft.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Service;

import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.domain.commission.draft.entity.enums.WatermarkStatus;
import ditda.backend.domain.commission.draft.repository.CommissionDraftFileRepository;
import ditda.backend.global.image.WatermarkImageProcessor;
import ditda.backend.global.s3.enums.BucketType;
import ditda.backend.global.s3.enums.S3ContentType;
import ditda.backend.global.s3.manager.S3FileManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DraftWatermarkService {

	private static final BucketType BUCKET = BucketType.PRIVATE;
	private static final String WATERMARK_DIR = "wm";

	private final CommissionDraftFileRepository commissionDraftFileRepository;
	private final WatermarkImageProcessor watermarkImageProcessor;
	private final S3FileManager s3FileManager;
	private final DraftWatermarkTransitionService draftWatermarkTransitionService;

	public void watermarkDraftFiles(Long draftId) {

		List<CommissionDraftFile> files =
			commissionDraftFileRepository.findAllByCommissionDraftIdAndWatermarkStatus(
				draftId,
				WatermarkStatus.PROCESSING
			);

		for (CommissionDraftFile file : files) {
			try {
				String watermarkedKey = createWatermarked(file.getFileUrl());
				draftWatermarkTransitionService.complete(file.getId(), watermarkedKey);
			} catch (Exception exception) {
				log.error("워터마크 처리 실패. draftFileId={}, fileUrl={}", file.getId(), file.getFileUrl(), exception);
				draftWatermarkTransitionService.fail(file.getId());
			}
		}
	}

	private String createWatermarked(String originalKey) throws IOException {

		byte[] watermarked;
		// 원본 s3 다운로드 후 워터마크 진행
		try (InputStream original = s3FileManager.download(BUCKET, originalKey)) {
			watermarked = watermarkImageProcessor.createWatermarkedPreview(original);
		}

		// 워터마크 진행된 파일 s3 업로드 (commission/draft/{uuid}.png -> commission/draft/wm/{uuid}.png)
		int lastSlash = originalKey.lastIndexOf('/');
		if (lastSlash < 0) {
			throw new IllegalArgumentException("유효하지 않은 S3 키: " + originalKey);
		}
		String dir = originalKey.substring(0, lastSlash);
		String filename = originalKey.substring(lastSlash + 1);
		int lastDot = filename.lastIndexOf('.');
		String baseName = lastDot > 0 ? filename.substring(0, lastDot) : filename;
		String watermarkedKey = dir + "/" + WATERMARK_DIR + "/" + baseName + ".png";

		s3FileManager.upload(BUCKET, watermarkedKey, watermarked, S3ContentType.PNG.getContentType());

		return watermarkedKey;
	}
}
