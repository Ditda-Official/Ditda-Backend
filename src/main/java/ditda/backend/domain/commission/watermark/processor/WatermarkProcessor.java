package ditda.backend.domain.commission.watermark.processor;

/**
 * 워터마크 처리 주체 추상화.
 * 구현체는 처리 완료/실패 시 상태 전이까지 책임진다.
 * - local: 서버 내 처리 (LocalWatermarkProcessor)
 * - lambda: AWS Lambda 위임 (LambdaWatermarkProcessor)
 */
public interface WatermarkProcessor {

	void process(Long draftFileId, String originalKey);
}
