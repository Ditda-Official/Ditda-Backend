package ditda.backend.global.s3;

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class S3UrlResolver {

	private final S3Properties s3Properties;

	public String toPublicS3Url(String key) {
		String[] segments = Arrays.stream(key.split("/"))
			.filter(s -> !s.isEmpty())
			.toArray(String[]::new);

		return UriComponentsBuilder.fromUriString(s3Properties.getPublicBaseUrl())
			.pathSegment(segments)
			.build()
			.toUriString();
	}
}
