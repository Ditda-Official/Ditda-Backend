package ditda.backend.global.image;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.stereotype.Component;

import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.image.exception.ImageErrorCode;

@Component
public class WatermarkImageProcessor {

	private static final String LOGO_PATH = "/images/watermark-logo.png";	// 워터마크 이미지 파일 경로
	private static final int TARGET_LONG_SIDE = 1600; 						// 출력물의 최대 길이
	private static final long MAX_PIXELS = 200_000_000L;					// 이미지 픽셀 제한
	private static final float OPACITY = 0.25f; 							// 워터마크 투명도
	private static final double ROTATION_DEGREES = -45; 					// 워터마크 텍스트 기울기
	private static final int LOGO_WIDTH_RATIO = 15;						 	// 로고 폭

	private final BufferedImage logo = loadLogo();

	public byte[] createWatermarkedPreview(InputStream source) throws IOException {

		// 1. 이미지 디코딩
		BufferedImage preview = readSubsampled(source);

		// 2. 워터마크 처리
		drawWatermark(preview);

		// 3. PNG로 재압축
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(preview, "png", out);

		// 4. S3 업로드용 바이트
		return out.toByteArray();
	}

	// 워터마크 로고 로드
	private BufferedImage loadLogo() {

		try (InputStream logoStream = getClass().getResourceAsStream(LOGO_PATH)) {
			if (logoStream == null) {
				throw new IllegalStateException("워터마크 로고 리소스가 없습니다: " + LOGO_PATH);
			}

			return ImageIO.read(logoStream);
		} catch (IOException exception) {
			throw new IllegalStateException("워터마크 로고 로드 실패: " + LOGO_PATH, exception);
		}

	}

	// 서브샘플링
	private BufferedImage readSubsampled(InputStream source) throws IOException {

		try (ImageInputStream iis = ImageIO.createImageInputStream(source)) {
			Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

			// 이미지가 아니거나 심하게 손상될 경우
			if (!readers.hasNext()) {
				throw new GeneralException(ImageErrorCode.IMAGE_NOT_READABLE);
			}

			ImageReader reader = readers.next();
			try {
				reader.setInput(iis);

				int width = reader.getWidth(0);
				int height = reader.getHeight(0);

				// 픽셀 수 계산
				if ((long)width * height > MAX_PIXELS) {
					throw new GeneralException(ImageErrorCode.IMAGE_RESOLUTION_EXCEEDED);
				}

				// 샘플링 간격
				int sample = Math.max(1, Math.max(width, height) / TARGET_LONG_SIDE);

				ImageReadParam param = reader.getDefaultReadParam();
				param.setSourceSubsampling(sample, sample, 0, 0);

				return reader.read(0, param);
			} finally {
				reader.dispose();
			}
		}
	}

	// 로고 기반 워터마크
	private void drawWatermark(BufferedImage image) {

		int logoWidth = image.getWidth() / LOGO_WIDTH_RATIO;
		int logoHeight = logoWidth * logo.getHeight() / logo.getWidth();   // 원본 비율 유지

		Graphics2D graphics = image.createGraphics();
		try {
			// 축소+회전을 한 번의 고품질 리샘플링으로 처리
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, OPACITY));
			graphics.rotate(
				Math.toRadians(ROTATION_DEGREES),
				image.getWidth() / 2.0,
				image.getHeight() / 2.0
			);

			int stepX = logoWidth * 4;        // 타일 가로 간격
			int stepY = logoHeight * 5;       // 타일 세로 간격

			for (int y = -image.getHeight(); y < image.getHeight() * 2; y += stepY) {
				for (int x = -image.getWidth(); x < image.getWidth() * 2; x += stepX) {
					graphics.drawImage(logo, x, y, logoWidth, logoHeight, null);
				}
			}
		} finally {
			graphics.dispose();
		}
	}
}
