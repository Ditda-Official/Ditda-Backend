package ditda.backend.global.s3.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.global.apipayload.response.ApiResponse;
import ditda.backend.global.s3.PresignedUpload;
import ditda.backend.global.s3.S3FileService;
import ditda.backend.global.s3.dto.request.PresignRequest;
import ditda.backend.global.s3.dto.response.PresignResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "File", description = "공용 파일 업로드 API")
public class S3FileController {

	private final S3FileService s3FileService;

	@Operation(
		summary = "파일 업로드 URL 발급",
		description = "업로드 대상에 맞는 presigned PUT URL을 발급합니다. "
			+ "PUT URL로 S3에 직접 업로드한 뒤, 응답의 key를 각 도메인 요청에 담아 전송합니다."
	)
	@PostMapping("/presigned-url")
	public ApiResponse<PresignResponse> issuePresignedUrl(
		@Valid @RequestBody PresignRequest request
	) {

		PresignedUpload upload = s3FileService.issuePresignedUpload(request.target(), request.contentType());
		return ApiResponse.onSuccess("파일 업로드 URL 발급 성공", PresignResponse.from(upload));
	}
}
