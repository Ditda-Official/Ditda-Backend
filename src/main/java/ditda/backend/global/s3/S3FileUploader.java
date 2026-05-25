package ditda.backend.global.s3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import ditda.backend.global.s3.exception.S3UploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3FileUploader {

	private final S3Client s3Client;
	private final S3Properties s3Properties;

	public String upload(String prefix, MultipartFile file) {
		String key = generateKey(prefix, file);

		try {
			s3Client.putObject(
				req -> req.bucket(s3Properties.getBucket())
					.key(key)
					.contentType(file.getContentType()),
				RequestBody.fromInputStream(file.getInputStream(), file.getSize())
			);
			return key;
		} catch (IOException | SdkException e) {
			log.error("Failed to upload file to S3. originalName={}", file.getOriginalFilename(), e);
			throw new S3UploadException(file.getOriginalFilename(), e);
		}
	}

	public List<String> uploadAll(String prefix, List<MultipartFile> files) {
		if (files == null || files.isEmpty()) {
			return List.of();
		}

		List<String> uploadedKeys = new ArrayList<>();
		try {
			for (MultipartFile file : files) {
				if (file.isEmpty()) {
					continue;
				}
				uploadedKeys.add(upload(prefix, file));
			}
			return uploadedKeys;
		} catch (Exception e) {
			// 부분 업로드된 파일 보상 삭제
			deleteAll(uploadedKeys);
			throw e;
		}
	}

	public void deleteAll(List<String> keys) {
		if (keys == null || keys.isEmpty()) {
			return;
		}
		for (String key : keys) {
			try {
				s3Client.deleteObject(req -> req.bucket(s3Properties.getBucket()).key(key));
			} catch (Exception e) {
				log.error("S3 delete failed. key={}", key, e);
			}
		}
	}

	private String generateKey(String prefix, MultipartFile file) {
		String extension = extractExtension(file.getOriginalFilename());
		return "%s/%s%s".formatted(prefix, UUID.randomUUID(), extension);
	}

	// 파일명에서 확장자 추출
	private String extractExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			return "";
		}
		return filename.substring(filename.lastIndexOf('.'));
	}
}
