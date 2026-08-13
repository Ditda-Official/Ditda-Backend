package ditda.backend.global.jwt.handler;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import ditda.backend.global.apipayload.code.GeneralErrorCode;
import ditda.backend.global.apipayload.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

	private final JsonMapper jsonMapper;

	@Override
	public void handle(
		HttpServletRequest request,
		HttpServletResponse response,
		AccessDeniedException accessDeniedException
	) throws IOException, ServletException {

		GeneralErrorCode errorCode = GeneralErrorCode.FORBIDDEN;

		log.warn("Access denied.");

		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write(jsonMapper.writeValueAsString(ApiResponse.onFailure(errorCode, null)));
	}
}
