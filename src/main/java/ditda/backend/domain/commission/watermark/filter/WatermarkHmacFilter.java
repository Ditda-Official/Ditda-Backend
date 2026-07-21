package ditda.backend.domain.commission.watermark.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import ditda.backend.domain.commission.watermark.processor.WatermarkCallbackVerifier;
import ditda.backend.global.apipayload.code.BaseErrorCode;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.apipayload.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class WatermarkHmacFilter extends OncePerRequestFilter {

	private static final String CALLBACK_PATH = "/api/v1/internal/watermarks/callback";

	private final WatermarkCallbackVerifier verifier;
	private final ObjectMapper objectMapper;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain chain
	) throws ServletException, IOException {

		CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);

		try {
			verifier.verify(
				request.getHeader("X-Signature"),
				request.getHeader("X-Timestamp"),
				cachedRequest.getBody()
			);
		} catch (GeneralException exception) {
			writeError(response, exception.getErrorCode());
			return;
		}

		chain.doFilter(cachedRequest, response);
	}

	private void writeError(HttpServletResponse response, BaseErrorCode code) throws IOException {

		response.setStatus(code.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		objectMapper.writeValue(response.getWriter(), ApiResponse.onFailure(code, code.getMessage()));
	}

	// 콜백 POST 경로만 검증
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !(HttpMethod.POST.matches(request.getMethod())
			&& CALLBACK_PATH.equals(request.getRequestURI()));
	}
}
