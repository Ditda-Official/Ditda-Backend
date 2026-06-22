package ditda.backend.global.s3.exception;

import org.springframework.http.HttpStatus;

import ditda.backend.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum S3ErrorCode implements BaseErrorCode {

	// 업로드 파일 검증 에러
	INVALID_FILE(HttpStatus.BAD_REQUEST, "S3_400_01", "유효하지 않거나 업로드되지 않은 파일입니다."),
	FILE_SIZE_EXCEEDED(HttpStatus.CONTENT_TOO_LARGE, "S3_413_01", "파일 크기가 제한을 초과했습니다."),
	UNSUPPORTED_CONTENT_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "S3_415_01", "지원하지 않는 파일 형식입니다."),

	// presigned URL 발급 에러
	FILE_URL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_500_01", "파일 URL 생성에 실패했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
