package ditda.backend.global.logging;

import java.io.IOException;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		MDC.put(MdcKey.REQUEST_URI, request.getRequestURI());
		MDC.put(MdcKey.HTTP_METHOD, request.getMethod());
		MDC.put(MdcKey.CLIENT_IP, getClientIp(request));

		try {
			filterChain.doFilter(request, response);
		} finally {
			MDC.remove(MdcKey.REQUEST_URI);
			MDC.remove(MdcKey.HTTP_METHOD);
			MDC.remove(MdcKey.CLIENT_IP);
		}
	}

	// 클라이언트의 실제 IP 획득
	private String getClientIp(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");

		if (StringUtils.hasText(forwarded)) {
			for (String ip : forwarded.split(",")) {
				String trimmedIp = ip.trim();
				if (!trimmedIp.isEmpty() && !"unknown".equalsIgnoreCase(trimmedIp)) {
					return trimmedIp;
				}
			}
		}

		String proxyClientIp = request.getHeader("Proxy-Client-IP");
		if (StringUtils.hasText(proxyClientIp) && !"unknown".equalsIgnoreCase(proxyClientIp)) {
			return proxyClientIp;
		}
		return request.getRemoteAddr();
	}
}
