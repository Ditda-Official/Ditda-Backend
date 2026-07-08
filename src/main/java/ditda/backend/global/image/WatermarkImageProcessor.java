package ditda.backend.global.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
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

import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.image.exception.ImageErrorCode;

@Component
public class WatermarkImageProcessor {

	private static final String WATERMARK_TEXT = "DITDA"; // 워터마크 텍스트
	private static final int TARGET_LONG_SIDE = 1600; // 출력물의 최대 길이
	private static final long MAX_PIXELS = 200_000_000L; // 이미지 픽셀 제한
	private static final float OPACITY = 0.15f; // 워터마크 투명도
	private static final double ROTATION_DEGREES = -30; // 워터마크 텍스트 기울기

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

	// 텍스트 워터마크
	private void drawWatermark(BufferedImage image) {

		Graphics2D graphics = image.createGraphics();

		try {
			// 안티앨리어싱 (텍스트만 부드럽게 처리)
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			// 기존 픽셀에 Opacity에 맞게 덧그리기
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, OPACITY));
			// 색상
			graphics.setColor(Color.gray);
			// 폰트
			graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, image.getWidth() / 8));
			// 좌표 회전 (글씨 대각선)
			graphics.rotate(Math.toRadians(ROTATION_DEGREES), image.getWidth() / 2.0, image.getHeight() / 2.0);

			FontMetrics fm = graphics.getFontMetrics();
			int stepX = fm.stringWidth(WATERMARK_TEXT) * 2;        // 글자폭
			int stepY = fm.getHeight() * 3;                        // 글자 높이

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
