package ditda.backend.global.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
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

import ditda.backend.global.image.dto.WatermarkedImage;
import ditda.backend.global.image.exception.ImageErrorCode;
import ditda.backend.global.image.exception.ImageProcessingException;
import ditda.backend.global.s3.enums.S3ContentType;

@Component
public class WatermarkImageProcessor {

	private static final String FONT_PATH = "/fonts/LINESeedSans_Bd.ttf";	// 워터마크 폰트 파일
	private static final String WATERMARK_TEXT = "ditda";					// 워터마크 텍스트
	private static final int TARGET_LONG_SIDE = 1600; 						// 출력물의 최대 길이
	private static final long MAX_PIXELS = 200_000_000L;					// 이미지 픽셀 제한
	private static final float OPACITY = 0.25f;								// 워터마크 투명도
	private static final double ROTATION_DEGREES = -45; 					// 워터마크 텍스트 기울기
	private static final int FONT_SIZE_RATIO = 32;							// 폰트 크기 (이미지 폭 대비 비율)
	private static final Color WATERMARK_COLOR = new Color(0xF5F5F5);	// 폰트 색상 (White Smoke)

	private final Font baseFont = loadFont();

	public WatermarkedImage createWatermarkedPreview(InputStream source) throws IOException {

		// 1. 이미지 디코딩
		BufferedImage preview = readSubsampled(source);

		// 2. 워터마크 처리
		drawWatermark(preview);

		// 3. PNG로 재압축
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		if (!ImageIO.write(preview, "png", out)) {
			throw new IOException("PNG 인코딩에 실패했습니다.");
		}

		// 4. S3 업로드용 바이트
		return new WatermarkedImage(out.toByteArray(), S3ContentType.PNG);
	}

	// 워터마크 폰트 로드 (1pt 로드 후 이미지 비율마다 변경)
	private Font loadFont() {

		try (InputStream fontStream = getClass().getResourceAsStream(FONT_PATH)) {
			if (fontStream == null) {
				throw new IllegalStateException("워터마크 폰트 리소스가 없습니다: " + FONT_PATH);
			}

			return Font.createFont(Font.TRUETYPE_FONT, fontStream);
		} catch (IOException | FontFormatException exception) {
			throw new IllegalStateException("워터마크 폰트 로드 실패: " + FONT_PATH, exception);
		}
	}

	// 서브샘플링
	private BufferedImage readSubsampled(InputStream source) throws IOException {

		try (ImageInputStream iis = ImageIO.createImageInputStream(source)) {
			Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

			// 이미지가 아니거나 심하게 손상될 경우
			if (!readers.hasNext()) {
				throw new ImageProcessingException(ImageErrorCode.IMAGE_NOT_READABLE);
			}

			ImageReader reader = readers.next();
			try {
				reader.setInput(iis);

				int width = reader.getWidth(0);
				int height = reader.getHeight(0);

				// 픽셀 수 계산
				if ((long)width * height > MAX_PIXELS) {
					throw new ImageProcessingException(ImageErrorCode.IMAGE_RESOLUTION_EXCEEDED);
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

	// 텍스트 기반 워터마크
	private void drawWatermark(BufferedImage image) {

		Graphics2D graphics = image.createGraphics();

		try {
			// 안티앨리어싱
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			// 덧그리기
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, OPACITY));
			// 색상
			graphics.setColor(WATERMARK_COLOR);
			// 폰트
			float fontSize = Math.max(1f, image.getWidth() / (float)FONT_SIZE_RATIO);
			graphics.setFont(baseFont.deriveFont(fontSize));
			// 좌표 회전
			graphics.rotate(Math.toRadians(ROTATION_DEGREES), image.getWidth() / 2.0, image.getHeight() / 2.0);

			FontMetrics fm = graphics.getFontMetrics();
			int stepX = Math.max(1, fm.stringWidth(WATERMARK_TEXT) * 2);		// 타일 가로 간격
			int stepY = Math.max(1, fm.getHeight() * 4);						// 타일 세로 간격

			for (int y = -image.getHeight(); y < image.getHeight() * 2; y += stepY) {
				for (int x = -image.getWidth(); x < image.getWidth() * 2; x += stepX) {
					graphics.drawString(WATERMARK_TEXT, x, y);
				}
			}
		} finally {
			graphics.dispose();
		}
	}
}
