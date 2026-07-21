package ditda.backend.global.logging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MdcKey {

	public static final String TRACE_ID = "traceId";

	public static final String USER_ID = "userId";
	public static final String CLIENT_IP = "clientIp";
	public static final String REQUEST_URI = "requestUri";
	public static final String HTTP_METHOD = "httpMethod";
}
