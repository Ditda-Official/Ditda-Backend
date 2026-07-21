package ditda.backend.domain.commission.watermark.exception;

import org.springframework.http.HttpStatus;

import ditda.backend.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WatermarkCallbackErrorCode implements BaseErrorCode {

	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "WATERMARK_CALLBACK_400_01", "유효하지 않은 콜백 요청입니다."),
	EXPIRED_TIMESTAMP(HttpStatus.UNAUTHORIZED, "WATERMARK_CALLBACK_401_01", "콜백 타임스탬프가 만료되었습니다."),
	INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "WATERMARK_CALLBACK_401_02", "콜백 서명이 유효하지 않습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
