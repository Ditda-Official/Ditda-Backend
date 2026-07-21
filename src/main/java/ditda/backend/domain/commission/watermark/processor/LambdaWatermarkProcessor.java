package ditda.backend.domain.commission.watermark.processor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.watermark.config.WatermarkProperties;
import ditda.backend.domain.commission.watermark.dto.WatermarkInvokePayload;
import ditda.backend.domain.commission.watermark.service.DraftWatermarkTransitionService;
import ditda.backend.global.s3.config.S3Properties;
import ditda.backend.global.s3.enums.BucketType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "watermark.mode", havingValue = "lambda")
public class LambdaWatermarkProcessor implements WatermarkProcessor {

	private final LambdaClient lambdaClient;
	private final WatermarkKeyResolver watermarkKeyResolver;
	private final DraftWatermarkTransitionService draftWatermarkTransitionService;
	private final S3Properties s3Properties;
	private final WatermarkProperties watermarkProperties;
	private final ObjectMapper objectMapper;

	@Override
	public void process(Long draftFileId, String originalKey) {

		try {
			String outputKey = watermarkKeyResolver.resolve(originalKey);

			WatermarkInvokePayload payload = new WatermarkInvokePayload(
				draftFileId,
				s3Properties.getBucket(BucketType.PRIVATE),
				originalKey,
				outputKey,
				watermarkProperties.lambda().callbackUrl()
			);

			InvokeRequest request = InvokeRequest.builder()
				.functionName(watermarkProperties.lambda().functionName())
				.invocationType(InvocationType.EVENT)
				.payload(SdkBytes.fromUtf8String(objectMapper.writeValueAsString(payload)))
				.build();

			lambdaClient.invoke(request);
			log.info("워터마크 Lambda 호출. draftFileId={}, outputKey={}", draftFileId, outputKey);
		} catch (Exception exception) {
			log.error("워터마크 Lambda 호출 실패. draftFileId={}", draftFileId, exception);
			draftWatermarkTransitionService.fail(draftFileId);
		}
	}
}
