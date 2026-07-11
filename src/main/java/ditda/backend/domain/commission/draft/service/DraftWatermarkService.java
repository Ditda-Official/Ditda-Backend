package ditda.backend.domain.commission.draft.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.domain.commission.draft.entity.enums.WatermarkStatus;
import ditda.backend.domain.commission.draft.repository.CommissionDraftFileRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.image.WatermarkImageProcessor;
import ditda.backend.global.image.dto.WatermarkedImage;
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

		files.forEach(f -> process(f.getId(), f.getFileUrl()));
	}

	// 워터마크 재처리
	@Async("watermarkExecutor")
	public void reprocessFile(Long draftFileId) {

		String originalKey = draftWatermarkTransitionService.getOriginalKey(draftFileId);
		process(draftFileId, originalKey);
	}

	private String createWatermarked(String originalKey) throws IOException {

		byte[] watermarked;
		// 원본 s3 다운로드 후 워터마크 진행
		try (InputStream original = s3FileManager.download(BUCKET, originalKey)) {
			WatermarkedImage image = watermarkImageProcessor.createWatermarkedPreview(original);
			watermarked = image.bytes();
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

	private void process(Long fileId, String originalKey) {

		long start = System.nanoTime();
		try {
			String watermarkedKey = createWatermarked(originalKey);
			draftWatermarkTransitionService.complete(fileId, watermarkedKey);
			log.info("워터마크 완료. draftFileId={}, elapsedMs={}", fileId, elapsedMs(start));
		} catch (GeneralException e) {
			log.error("워터마크 영구 실패(이미지 문제). draftFileId={}", fileId, e);
			draftWatermarkTransitionService.failPermanently(fileId);
		} catch (Exception e) {
			log.error("워터마크 실패. draftFileId={}, elapsedMs={}", fileId, elapsedMs(start), e);
			draftWatermarkTransitionService.fail(fileId);
		}
	}

	private long elapsedMs(long start) {
		return (System.nanoTime() - start) / 1_000_000;
	}
}
