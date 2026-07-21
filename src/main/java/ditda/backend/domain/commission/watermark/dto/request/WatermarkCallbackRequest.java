package ditda.backend.domain.commission.watermark.dto.request;

public record WatermarkCallbackRequest(
	Long draftFileId,
	Result result,
	String watermarkedKey,
	String errorCode
) {

	public enum Result {
		COMPLETED,
		FAILED_PERMANENT,
		FAILED_TRANSIENT
	}
}
