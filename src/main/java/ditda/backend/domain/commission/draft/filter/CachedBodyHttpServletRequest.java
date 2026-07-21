package ditda.backend.domain.commission.draft.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.springframework.util.StreamUtils;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

	private final byte[] cachedBody;

	public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
		super(request);
		this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
	}

	public String getBody() {
		return new String(cachedBody, StandardCharsets.UTF_8);
	}

	@Override
	public ServletInputStream getInputStream() {
		ByteArrayInputStream bais = new ByteArrayInputStream(cachedBody);
		return new ServletInputStream() {
			@Override
			public int read() {
				return bais.read();
			}

			@Override
			public boolean isFinished() {
				return bais.available() == 0;
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setReadListener(ReadListener listener) {
			}
		};
	}

	@Override
	public BufferedReader getReader() {
		return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cachedBody), StandardCharsets.UTF_8));
	}
}
