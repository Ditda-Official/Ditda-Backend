package ditda.backend.domain.commission.watermark.processor;

import org.springframework.stereotype.Component;

@Component
public class WatermarkKeyResolver {

	private static final String WATERMARK_DIR = "wm";

	public String resolve(String originalKey) {

		// 워터마크 출력 키 생성
		int lastSlash = originalKey.lastIndexOf('/');
		if (lastSlash < 0) {
			throw new IllegalArgumentException("유효하지 않은 S3 키: " + originalKey);
		}
		String dir = originalKey.substring(0, lastSlash);
		String filename = originalKey.substring(lastSlash + 1);
		int lastDot = filename.lastIndexOf('.');
		String baseName = lastDot > 0 ? filename.substring(0, lastDot) : filename;

		return dir + "/" + WATERMARK_DIR + "/" + baseName + ".png";
	}
}
