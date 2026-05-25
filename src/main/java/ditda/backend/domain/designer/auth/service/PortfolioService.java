package ditda.backend.domain.designer.auth.service;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import ditda.backend.domain.common.user.entity.User;
import ditda.backend.domain.designer.auth.entity.Portfolio;
import ditda.backend.domain.designer.auth.exception.DesignerErrorCode;
import ditda.backend.domain.designer.auth.repository.PortfolioRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import ditda.backend.global.s3.S3FileUploader;
import ditda.backend.global.s3.exception.S3UploadException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioService {

	private static final int MAX_PORTFOLIO_COUNT = 3;
	private static final String S3_KEY_PREFIX = "portfolio";
	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
		"application/pdf",
		"image/png"
	);

	private final S3FileUploader s3FileUploader;
	private final PortfolioRepository portfolioRepository;

	@Value("${spring.servlet.multipart.max-file-size}")
	private DataSize maxFileSize;

	public void validateFiles(List<MultipartFile> files) {
		if (files == null || files.isEmpty()) {
			return;
		}

		// 파일 최대 개수 검증
		long realCount = files.stream().filter(f -> !f.isEmpty()).count();
		if (realCount > MAX_PORTFOLIO_COUNT) {
			throw new GeneralException(DesignerErrorCode.PORTFOLIO_FILE_LIMIT_EXCEEDED);
		}

		// 파일 사이즈 및 타입 검증
		for (MultipartFile file : files) {
			if (file.isEmpty()) {
				continue;
			}

			if (file.getSize() > maxFileSize.toBytes()) {
				throw new GeneralException(DesignerErrorCode.PORTFOLIO_FILE_LIMIT_EXCEEDED);
			}
			if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
				throw new GeneralException(DesignerErrorCode.INVALID_PORTFOLIO_FILE);
			}
		}
	}

	public List<String> uploadFiles(List<MultipartFile> files) {
		try {
			return s3FileUploader.uploadAll(S3_KEY_PREFIX, files);
		} catch (S3UploadException e) {
			throw new GeneralException(DesignerErrorCode.PORTFOLIO_UPLOAD_FAILED);
		}
	}

	@Transactional
	public void savePortfolios(User user, List<String> portfolioKeys) {
		if (portfolioKeys.isEmpty()) {
			return;
		}

		// 파일을 DB에 일괄 저장
		List<Portfolio> portfolios = portfolioKeys.stream()
			.map(key -> Portfolio.createPortfolio(user, key))
			.toList();
		portfolioRepository.saveAll(portfolios);
	}

	public void deleteFiles(List<String> portfolioKeys) {
		s3FileUploader.deleteAll(portfolioKeys);
	}
}
