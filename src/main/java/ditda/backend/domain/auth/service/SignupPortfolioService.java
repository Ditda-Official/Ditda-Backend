package ditda.backend.domain.auth.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.auth.dto.response.PortfolioPresignResponse;
import ditda.backend.domain.auth.exception.PortfolioErrorCode;
import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.domain.designer.entity.Portfolio;
import ditda.backend.domain.designer.repository.PortfolioRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.s3.PresignedUpload;
import ditda.backend.global.s3.S3Properties;
import ditda.backend.global.s3.S3UploadManager;
import ditda.backend.global.s3.enums.BucketType;
import ditda.backend.global.s3.enums.S3ContentType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignupPortfolioService {

	private static final int MAX_PORTFOLIO_COUNT = 3;
	private static final String DIR = "portfolio";
	private static final BucketType BUCKET = BucketType.PRIVATE;
	private static final Set<S3ContentType> ALLOWED = EnumSet.of(
		S3ContentType.PNG,
		S3ContentType.PDF
	);

	private final S3UploadManager s3UploadManager;
	private final PortfolioRepository portfolioRepository;
	private final S3Properties s3Properties;

	public PortfolioPresignResponse generatePresignedUpload(String contentType) {

		// 파일 타입 검증
		S3ContentType type = S3ContentType.from(contentType);
		if (type == null || !ALLOWED.contains(type)) {
			throw new GeneralException(PortfolioErrorCode.INVALID_PORTFOLIO_FILE);
		}

		PresignedUpload upload = s3UploadManager.issueTempUpload(BUCKET, DIR, type.getExtension(), contentType);
		return new PortfolioPresignResponse(upload.key(), upload.presignedUrl());
	}

	public void validateKeys(List<String> keys) {

		// 파일 개수 검증
		if (keys.size() > MAX_PORTFOLIO_COUNT) {
			throw new GeneralException(PortfolioErrorCode.PORTFOLIO_FILE_LIMIT_EXCEEDED);
		}

		// distinct key 검증
		if (keys.size() != keys.stream().distinct().count()) {
			throw new GeneralException(PortfolioErrorCode.INVALID_PORTFOLIO_FILE);
		}

		for (String key : keys) {
			// 파일 key 형식 검증
			if (!s3UploadManager.isTempKey(key, DIR)) {
				throw new GeneralException(PortfolioErrorCode.INVALID_PORTFOLIO_FILE);
			}

			// 파일 크기 검증
			Long size = s3UploadManager.getObjectSize(BUCKET, key);
			if (size == null) {
				throw new GeneralException(PortfolioErrorCode.INVALID_PORTFOLIO_FILE);    // 미업로드 key
			}
			if (size > s3Properties.getMaxFileSize().toBytes()) {
				throw new GeneralException(PortfolioErrorCode.PORTFOLIO_FILE_SIZE_EXCEEDED);
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

	public List<String> promote(List<String> tempKeys) {
		return s3UploadManager.promote(BUCKET, tempKeys);
	}

	public void deleteFiles(List<String> portfolioKeys) {
		s3UploadManager.deleteAll(BUCKET, portfolioKeys);
	}

}
