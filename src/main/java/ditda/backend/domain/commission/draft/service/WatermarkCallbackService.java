package ditda.backend.domain.commission.draft.service;

import org.springframework.stereotype.Service;

import ditda.backend.domain.commission.draft.dto.request.WatermarkCallbackRequest;
import ditda.backend.domain.commission.draft.exception.WatermarkCallbackErrorCode;
import ditda.backend.domain.commission.draft.processor.WatermarkCallbackVerifier;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

// Lambda 워터마크 콜백 처리 (검증 → 파싱 → 상태 전이)
@Slf4j
@Service
@RequiredArgsConstructor
public class WatermarkCallbackService {

	private final WatermarkCallbackVerifier watermarkCallbackVerifier;
	private final DraftWatermarkTransitionService draftWatermarkTransitionService;
	private final ObjectMapper objectMapper;

	public void handleCallback(String signature, String timestamp, String rawBody) {

		// 1. HMAC 검증
		watermarkCallbackVerifier.verify(signature, timestamp, rawBody);

		// 2. 검증된 body 파싱
		WatermarkCallbackRequest request = parse(rawBody);

		if (request.result() == null) {
			throw new GeneralException(WatermarkCallbackErrorCode.INVALID_REQUEST);
		}

		// 3. 결과에 따른 상태 전이
		switch (request.result()) {
			case COMPLETED ->
				draftWatermarkTransitionService.complete(request.draftFileId(), request.watermarkedKey());
			case FAILED_PERMANENT ->
				draftWatermarkTransitionService.failPermanently(request.draftFileId());
			case FAILED_TRANSIENT ->
				draftWatermarkTransitionService.fail(request.draftFileId());
		}

		log.info("워터마크 콜백 처리. draftFileId={}, result={}", request.draftFileId(), request.result());
	}

	private WatermarkCallbackRequest parse(String rawBody) {
		try {
			return objectMapper.readValue(rawBody, WatermarkCallbackRequest.class);
		} catch (JacksonException e) {
			throw new GeneralException(WatermarkCallbackErrorCode.INVALID_REQUEST);
		}
	}
}
