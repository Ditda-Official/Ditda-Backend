package ditda.backend.global.image;

import static org.assertj.core.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ditda.backend.global.apipayload.exception.GeneralException;

class WatermarkImageProcessorTest {

	private final WatermarkImageProcessor watermarkImageProcessor = new WatermarkImageProcessor();

	@Test
	@DisplayName("목표보다 큰 이미지는 긴 변이 목표 크기로 축소된다")
	void createWatermarkedPreview_downscalesLargeImage() throws IOException {

		// given: 긴 변이 목표(1600)의 2배인 원본
		byte[] source = pngBytes(3200, 2400);

		// when
		byte[] result = watermarkImageProcessor.createWatermarkedPreview(new ByteArrayInputStream(source));

		// then: sample=2로 절반 크기
		BufferedImage preview = ImageIO.read(new ByteArrayInputStream(result));
		assertThat(preview.getWidth()).isEqualTo(1600);
		assertThat(preview.getHeight()).isEqualTo(1200);
	}

	@Test
	@DisplayName("목표보다 작은 이미지는 축소 없이 처리된다")
	void createWatermarkedPreview_keepsSmallImageSize() throws IOException {

		// given
		byte[] source = pngBytes(800, 600);

		// when
		byte[] result = watermarkImageProcessor.createWatermarkedPreview(new ByteArrayInputStream(source));

		// then
		BufferedImage preview = ImageIO.read(new ByteArrayInputStream(result));
		assertThat(preview.getWidth()).isEqualTo(800);
		assertThat(preview.getHeight()).isEqualTo(600);
	}

	@Test
	@DisplayName("이미지가 아닌 파일은 예외가 발생한다")
	void createWatermarkedPreview_rejectsNonImage() {

		// given: png로 위장한 텍스트
		byte[] fake = "this is not an image".getBytes();

		// when & then
		assertThatThrownBy(() ->
			watermarkImageProcessor.createWatermarkedPreview(new ByteArrayInputStream(fake)))
			.isInstanceOf(GeneralException.class);
	}

	@Disabled("로컬 확인 및 처리 시간 측정용 - 입력 폴더 경로를 맞추고 @Disabled를 지운 뒤 실행")
	@Test
	void manualPreviewForEyeCheck() throws IOException {

		// 입력: 이 폴더 안의 모든 png / 출력: 하위 out 폴더에 같은 이름으로
		Path inputDir = Path.of("/Users/jong/Desktop/watermark-test");
		Path outputDir = inputDir.resolve("out");
		Files.createDirectories(outputDir);

		List<Path> sources;
		try (Stream<Path> paths = Files.list(inputDir)) {
			sources = paths
				.filter(path -> path.toString().endsWith(".png"))
				.sorted()
				.toList();
		}

		long totalStart = System.nanoTime();

		for (Path source : sources) {
			long start = System.nanoTime();

			byte[] result;
			try (InputStream in = Files.newInputStream(source)) {
				result = watermarkImageProcessor.createWatermarkedPreview(in);
			}
			Files.write(outputDir.resolve(source.getFileName()), result);

			long elapsedMs = (System.nanoTime() - start) / 1_000_000;
			System.out.printf("%s: 원본 %,dKB -> 결과 %,dKB, %,dms%n",
				source.getFileName(), Files.size(source) / 1024, result.length / 1024, elapsedMs);
		}

		long totalMs = (System.nanoTime() - totalStart) / 1_000_000;
		System.out.printf("총 %d개 처리, %,dms (평균 %,dms)%n",
			sources.size(), totalMs, sources.isEmpty() ? 0 : totalMs / sources.size());
	}

	private byte[] pngBytes(int width, int height) throws IOException {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(image, "png", out);
		return out.toByteArray();
	}
}
