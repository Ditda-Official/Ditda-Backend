package ditda.backend.global.apipayload.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GeneralErrorCode implements BaseErrorCode {

	// 인증 에러
	MISSING_AUTH_INFO(HttpStatus.UNAUTHORIZED, "AUTH_401_01", "인증 정보가 누락되었습니다."),
	INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "AUTH_401_02", "올바르지 않은 아이디, 혹은 비밀번호입니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_03", "유효하지 않은 토큰입니다."),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_401_04", "토큰이 만료되었습니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_403_01", "접근 권한이 없습니다."),

	// 요청/파라미터 에러
	MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "REQ_400_01", "필수 파라미터가 누락되었습니다."),
	INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "REQ_400_02", "파라미터 형식이 잘못되었습니다."),
	INVALID_BODY_TYPE(HttpStatus.BAD_REQUEST, "REQ_400_03", "요청 본문의 형식이 잘못되었거나, 허용되지 않은 값이 포함되어 있습니다."),
	INVALID_FILE(HttpStatus.BAD_REQUEST, "REQ_400_04", "유효하지 않거나 업로드되지 않은 파일입니다."),
	UNSUPPORTED_CONTENT_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "REQ_415_01", "지원하지 않는 Content-Type입니다."),
	DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "REQ_409_01", "이미 존재하는 데이터입니다."),
	FILE_SIZE_EXCEEDED(HttpStatus.CONTENT_TOO_LARGE, "REQ_413_01", "파일 크기가 제한을 초과했습니다."),

	// API/라우팅 에러
	API_NOT_FOUND(HttpStatus.NOT_FOUND, "API_404_01", "존재하지 않는 API입니다."),
	RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "REQ_404_02", "요청한 리소스를 찾을 수 없습니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "API_405_01", "지원하지 않는 HTTP 메서드입니다."),

	// 암호화/복호화 에러
	ENCRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ENCRYPT_500_01", "데이터 암호화에 실패했습니다."),
	DECRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ENCRYPT_500_02", "데이터 복호화에 실패했습니다."),
	HASHING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ENCRYPT_500_03", "데이터 해싱에 실패했습니다."),

	// 서버 내부 에러
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_500_01", "서버 내부 오류입니다."),
	SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "SERVER_503_01", "서버가 일시적으로 불안정합니다."),
	EXTERNAL_SERVICE_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "SERVER_504_01", "외부 서비스 응답 지연"),

	// S3 파일 에러
	FILE_URL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_500_01", "파일 URL 생성에 실패했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
