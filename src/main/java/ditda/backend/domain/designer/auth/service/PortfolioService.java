package ditda.backend.domain.designer.auth.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {

	private static final int MAX_PORTFOLIO_COUNT = 3;
	private static final String S3_KEY_PREFIX = "portfolio";
	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
		"application/pdf",
		"image/png"
	);

	private final S3Client s3Client;
	private final PortfolioRepository portfolioRepository;

	@Value("${app.s3.bucket}")
	private String bucket;

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
		if (files == null || files.isEmpty()) {
			return List.of();
		}

		// 파일을 S3에 업로드
		List<String> uploadedKeys = new ArrayList<>();
		try {
			for (MultipartFile file : files) {
				if (file.isEmpty()) {
					continue;
				}
				uploadedKeys.add(uploadToS3(file));
			}

			return uploadedKeys;
		} catch (Exception e) {
			// 부분 업로드된 파일 보상 삭제
			deleteFiles(uploadedKeys);
			throw e;
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
		if (portfolioKeys == null || portfolioKeys.isEmpty()) {
			return;
		}

		for (String key : portfolioKeys) {
			try {
				s3Client.deleteObject(req -> req.bucket(bucket).key(key));
			} catch (Exception e) {
				log.warn("Failed to delete S3 portfolio file. key={}", key, e);
			}
		}
	}

	private String uploadToS3(MultipartFile file) {
		String key = generateKey(file);
		try {
			s3Client.putObject(
				req -> req.bucket(bucket).key(key).contentType(file.getContentType()),
				RequestBody.fromInputStream(file.getInputStream(), file.getSize())
			);
			return key;
		} catch (IOException | SdkException e) {
			log.error("Failed to upload portfolio file. originalName={}", file.getOriginalFilename(), e);
			throw new GeneralException(DesignerErrorCode.PORTFOLIO_UPLOAD_FAILED);
		}
	}

	private String generateKey(MultipartFile file) {
		String extension = extractExtension(file.getOriginalFilename());
		return "%s/%s%s".formatted(S3_KEY_PREFIX, UUID.randomUUID(), extension);
	}

	// 파일명에 확장자 추출
	private String extractExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			return "";
		}
		return filename.substring(filename.lastIndexOf('.'));
	}
}
