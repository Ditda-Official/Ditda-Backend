package ditda.backend.domain.payment.exception;

import org.springframework.http.HttpStatus;

import ditda.backend.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentErrorCode implements BaseErrorCode {

	PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_404_01", "결제 정보를 찾을 수 없습니다."),
	PAYMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PAYMENT_403_01", "해당 외주에 대한 권한이 없습니다."),
	DEPOSIT_NOTIFY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PAYMENT_400_01", "입금 확인 대기 상태에서만 입금 통보가 가능합니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
