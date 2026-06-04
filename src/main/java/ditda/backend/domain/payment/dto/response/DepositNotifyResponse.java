package ditda.backend.domain.payment.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import ditda.backend.domain.payment.entity.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "입금 통보 응답")
public record DepositNotifyResponse(

	@Schema(description = "외주 ID", example = "1")
	Long commissionId,

	@Schema(description = "결제 상태", example = "PENDING")
	PaymentStatus status,

	@Schema(description = "입금 통보 시각", example = "2026-05-26 15:30:00")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	LocalDateTime depositNotifiedAt
) {

	public static DepositNotifyResponse of(
		Long commissionId,
		PaymentStatus status,
		LocalDateTime depositNotifiedAt
	) {
		return new DepositNotifyResponse(commissionId, status, depositNotifiedAt);
	}
}
