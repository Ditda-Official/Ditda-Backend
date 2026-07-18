package ditda.backend.domain.commission.draft.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ditda.backend.domain.commission.draft.service.WatermarkCallbackService;
import ditda.backend.global.apipayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/watermarks")
@RequiredArgsConstructor
@Tag(name = "Internal - Watermark Callback", description = "Lambda 워터마크 결과 콜백 (내부 전용)")
public class WatermarkCallbackController {

	private final WatermarkCallbackService watermarkCallbackService;

	@PostMapping("/callback")
	public ApiResponse<Void> handleCallback(
		@RequestHeader(value = "X-Signature", required = false) String signature,
		@RequestHeader(value = "X-Timestamp", required = false) String timestamp,
		@RequestBody String rawBody
	) {
		watermarkCallbackService.handleCallback(signature, timestamp, rawBody);
		return ApiResponse.onSuccess("워터마크 콜백 처리 완료");
	}
}
