package ditda.backend.domain.designer.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.domain.designer.entity.Portfolio;
import ditda.backend.domain.designer.exception.DesignerErrorCode;
import ditda.backend.domain.designer.repository.PortfolioRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.s3.dto.PresignedUpload;
import ditda.backend.global.s3.enums.BucketType;
import ditda.backend.global.s3.enums.UploadTarget;
import ditda.backend.global.s3.manager.S3UploadManager;
import ditda.backend.global.s3.service.S3FileService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioService {

	private static final int MAX_PORTFOLIO_COUNT = 3;
	private static final BucketType BUCKET = BucketType.PRIVATE;

	private final S3UploadManager s3UploadManager;
	private final S3FileService s3FileService;
	private final PortfolioRepository portfolioRepository;

	public PresignedUpload generatePresignedUpload(String contentType) {

		return s3FileService.issuePresignedUpload(UploadTarget.PORTFOLIO, contentType);
	}

	public void validateKeys(List<String> keys) {

		// 파일 개수 검증
		if (keys.size() > MAX_PORTFOLIO_COUNT) {
			throw new GeneralException(DesignerErrorCode.PORTFOLIO_FILE_LIMIT_EXCEEDED);
		}

		// distinct, 형식, 크기 검증
		s3FileService.validateUploadedKeys(UploadTarget.PORTFOLIO, keys);
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

	@Transactional(readOnly = true)
	public List<String> getPortfolioKeys(Long designerId) {
		return portfolioRepository.findByDesignerIdOrderByIdAsc(designerId).stream()
			.map(Portfolio::getPortfolioUrl)
			.toList();
	}

	public List<String> promote(List<String> tempKeys) {
		return s3UploadManager.promote(BUCKET, tempKeys);
	}

	public void deleteFiles(List<String> portfolioKeys) {
		s3UploadManager.deleteAll(BUCKET, portfolioKeys);
	}

}
