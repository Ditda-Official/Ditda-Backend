package ditda.backend.domain.commission.draft.processor;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.draft.service.DraftWatermarkTransitionService;
import ditda.backend.global.image.WatermarkImageProcessor;
import ditda.backend.global.image.dto.WatermarkedImage;
import ditda.backend.global.image.exception.ImageProcessingException;
import ditda.backend.global.s3.enums.BucketType;
import ditda.backend.global.s3.enums.S3ContentType;
import ditda.backend.global.s3.manager.S3FileManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "watermark.mode", havingValue = "local")
public class LocalWatermarkProcessor implements WatermarkProcessor {

	private static final BucketType BUCKET = BucketType.PRIVATE;

	private final WatermarkImageProcessor watermarkImageProcessor;
	private final S3FileManager s3FileManager;
	private final WatermarkKeyResolver watermarkKeyResolver;
	private final DraftWatermarkTransitionService draftWatermarkTransitionService;

	// 로컬 구현체의 경우에는 리소스 제한을 위해 단일 스레드로 진행
	@Async("watermarkExecutor")
	@Override
	public void process(Long draftFileId, String originalKey) {

		long start = System.nanoTime();
		try {
			String watermarkedKey = createWatermarked(originalKey);
			draftWatermarkTransitionService.complete(draftFileId, watermarkedKey);
			log.info("워터마크 완료. draftFileId={}, elapsedMs={}", draftFileId, elapsedMs(start));
		} catch (ImageProcessingException exception) {
			log.error("워터마크 영구 실패(이미지 문제). draftFileId={}", draftFileId, exception);
			draftWatermarkTransitionService.failPermanently(draftFileId);
		} catch (Exception e) {
			log.error("워터마크 실패. draftFileId={}, elapsedMs={}", draftFileId, elapsedMs(start), e);
			draftWatermarkTransitionService.fail(draftFileId);
		}
	}

	private String createWatermarked(String originalKey) throws IOException {

		byte[] watermarked;
		// 원본 s3 다운로드 후 워터마크 진행
		try (InputStream original = s3FileManager.download(BUCKET, originalKey)) {
			WatermarkedImage image = watermarkImageProcessor.createWatermarkedPreview(original);
			watermarked = image.bytes();
		}

		String watermarkedKey = watermarkKeyResolver.resolve(originalKey);
		s3FileManager.upload(BUCKET, watermarkedKey, watermarked, S3ContentType.PNG.getContentType());

		return watermarkedKey;
	}

	private long elapsedMs(long start) {
		return (System.nanoTime() - start) / 1_000_000;
	}
}
