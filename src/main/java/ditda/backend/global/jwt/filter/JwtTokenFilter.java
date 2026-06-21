package ditda.backend.global.jwt.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import ditda.backend.global.jwt.JwtTokenProvider;
import ditda.backend.global.jwt.dto.AccessTokenPayload;
import ditda.backend.global.jwt.exceptions.JwtErrorType;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		String token = resolveToken(request);

		if (StringUtils.hasText(token)) {
			try {
				AccessTokenPayload payload = jwtTokenProvider.getAccessTokenPayload(token);

				Authentication authentication =
					new UsernamePasswordAuthenticationToken(
						payload.userId(),
						null,
						List.of(new SimpleGrantedAuthority("ROLE_" + payload.role().name()))
					);

				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (ExpiredJwtException e) {
				request.setAttribute("exception", JwtErrorType.TOKEN_EXPIRED);
			} catch (JwtException | IllegalArgumentException e) {
				request.setAttribute("exception", JwtErrorType.INVALID_TOKEN);
			}
		}

		filterChain.doFilter(request, response);
	}

	private String resolveToken(HttpServletRequest request) {

		String bearerToken = request.getHeader("Authorization");

		// Swagger & Postman 전용
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}

		return null;
	}
}
