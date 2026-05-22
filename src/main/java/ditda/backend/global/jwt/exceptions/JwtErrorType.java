package ditda.backend.global.jwt.exceptions;


import ditda.backend.global.apipayload.code.GeneralErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JwtErrorType {
	TOKEN_EXPIRED(GeneralErrorCode.TOKEN_EXPIRED),
	INVALID_TOKEN(GeneralErrorCode.INVALID_TOKEN);

	private final GeneralErrorCode errorCode;
}
