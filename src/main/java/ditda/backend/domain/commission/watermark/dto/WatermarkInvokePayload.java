package ditda.backend.domain.commission.watermark.dto;

public record WatermarkInvokePayload(
	Long draftFileId,
	String bucket,
	String originalKey,
	String outputKey,
	String callbackUrl
) {
}
