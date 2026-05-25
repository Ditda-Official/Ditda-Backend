package ditda.backend.global.s3;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class S3UrlResolver {

	private final S3Properties s3Properties;

	public String toPublicS3Url(String key) {
		String baseUrl = s3Properties.getPublicBaseUrl();
		String trimmedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
		String trimmedKey = key.startsWith("/") ? key.substring(1) : key;
		return trimmedBase + "/" + trimmedKey;
	}
}
