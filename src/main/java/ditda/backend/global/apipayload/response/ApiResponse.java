package ditda.backend.global.apipayload.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ditda.backend.global.apipayload.code.BaseErrorCode;
import ditda.backend.global.apipayload.code.GeneralSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"success", "code", "message", "result", "error", "timestamp"})
public class ApiResponse<T> {

	@JsonProperty("success")
	private final boolean success;

	@JsonProperty("code")
	private final String code;

	@JsonProperty("message")
	private final String message;

	@JsonProperty("result")
	private final T result;

	@JsonProperty("error")
	private final Object error;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private final LocalDateTime timestamp;

	// result가 있는 성공 응답
	public static <T> ApiResponse<T> onSuccess(String message, T result) {
		return new ApiResponse<>(true, GeneralSuccessCode.OK.getCode(), message, result, null, LocalDateTime.now());
	}

	// result가 없는 성공 응답
	public static <T> ApiResponse<T> onSuccess(String message) {
		return new ApiResponse<>(true, GeneralSuccessCode.OK.getCode(), message, null, null, LocalDateTime.now());
	}

	// 실패 응답
	public static <T> ApiResponse<T> onFailure(BaseErrorCode errorCode, Object error) {
		return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null, error, LocalDateTime.now());
	}
}
