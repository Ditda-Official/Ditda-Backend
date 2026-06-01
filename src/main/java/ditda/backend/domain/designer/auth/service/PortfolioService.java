package ditda.backend.domain.designer.auth.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.designer.auth.dto.response.PortfolioPresignResponse;
import ditda.backend.domain.designer.auth.entity.Designer;
import ditda.backend.domain.designer.auth.entity.Portfolio;
import ditda.backend.domain.designer.auth.exception.DesignerErrorCode;
import ditda.backend.domain.designer.auth.repository.PortfolioRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.s3.S3FileManager;
import ditda.backend.global.s3.S3PresignedUrlGenerator;
import ditda.backend.global.s3.S3Properties;
import ditda.backend.global.s3.enums.BucketType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioService {

	private static final int MAX_PORTFOLIO_COUNT = 3;
	private static final String S3_KEY_PREFIX = "portfolio";
	private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
		"application/pdf", ".pdf",
		"image/png", ".png"
	);

	private final S3PresignedUrlGenerator s3PresignedUrlGenerator;
	private final S3FileManager s3FileManager;
	private final PortfolioRepository portfolioRepository;
	private final S3Properties s3Properties;

	public PortfolioPresignResponse generatePresignedUpload(String contentType) {

		// 파일 타입 검증
		String extension = ALLOWED_CONTENT_TYPES.get(contentType);
		if (extension == null) {
			throw new GeneralException(DesignerErrorCode.INVALID_PORTFOLIO_FILE);
		}

		String key = "%s/%s%s".formatted(S3_KEY_PREFIX, UUID.randomUUID(), extension);
		String presignedUrl = s3PresignedUrlGenerator.generatePrivatePutUrl(key, contentType);

		return new PortfolioPresignResponse(key, presignedUrl);
	}

	public void validateKeys(List<String> keys) {

		// 파일 개수 검증
		if (keys.size() > MAX_PORTFOLIO_COUNT) {
			throw new GeneralException(DesignerErrorCode.PORTFOLIO_FILE_LIMIT_EXCEEDED);
		}

		// distinct key 검증
		if (keys.size() != keys.stream().distinct().count()) {
			throw new GeneralException(DesignerErrorCode.INVALID_PORTFOLIO_FILE);
		}

		for (String key : keys) {
			// 파일 key 형식 검증
			if (key == null || !key.startsWith(S3_KEY_PREFIX + "/")) {
				throw new GeneralException(DesignerErrorCode.INVALID_PORTFOLIO_FILE);
			}

			// 파일 크기 검증
			Long size = s3FileManager.getObjectSize(BucketType.PRIVATE, key);
			if (size == null) {
				throw new GeneralException(DesignerErrorCode.INVALID_PORTFOLIO_FILE);    // 미업로드 key
			}
			if (size > s3Properties.getMaxFileSize().toBytes()) {
				throw new GeneralException(DesignerErrorCode.PORTFOLIO_FILE_SIZE_EXCEEDED);
			}
		}
	}

	@Transactional
	public void savePortfolios(Designer designer, List<String> portfolioKeys) {
		if (portfolioKeys.isEmpty()) {
			return;
		}

		// 파일을 DB에 일괄 저장
		List<Portfolio> portfolios = portfolioKeys.stream()
			.map(key -> Portfolio.createPortfolio(designer, key))
			.toList();
		portfolioRepository.saveAll(portfolios);
	}

	public void deleteFiles(List<String> portfolioKeys) {
		s3FileManager.deleteAll(BucketType.PRIVATE, portfolioKeys);
	}

}
