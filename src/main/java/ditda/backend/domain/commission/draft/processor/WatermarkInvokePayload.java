package ditda.backend.domain.commission.draft.processor;

public record WatermarkInvokePayload(
	Long draftFileId,
	String bucket,
	String originalKey,
	String outputKey,
	String callbackUrl
) {
}
