package ditda.backend.domain.commission.draft.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.domain.commission.draft.repository.CommissionDraftFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DraftWatermarkTransitionService {

	private final CommissionDraftFileRepository commissionDraftFileRepository;

	// 워터마크 완료 전이
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void complete(Long draftFileId, String watermarkedKey) {

		commissionDraftFileRepository.findById(draftFileId)
			.ifPresentOrElse(
				file -> file.completeWatermark(watermarkedKey),
				() -> log.warn("워터마크 완료 전이 대상 없음. draftFileId={}, key={}", draftFileId, watermarkedKey)
			);
	}

	// 영구 실패 전이 (이미지 문제)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void failPermanently(Long draftFileId) {

		commissionDraftFileRepository.findById(draftFileId)
			.ifPresentOrElse(file -> {
				file.markWatermarkFailedPermanently();
				notifyPermanentFailure(draftFileId);
			}, () -> log.warn("워터마크 영구 실패 전이 대상 없음. draftFileId={}", draftFileId));
	}

	// 재시도 실패 전이
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void fail(Long draftFileId) {

		commissionDraftFileRepository.findById(draftFileId)
			.ifPresentOrElse(file -> {
				file.markWatermarkFailed();
				if (!file.isWatermarkRetryable()) {
					notifyPermanentFailure(draftFileId);
				}
			}, () -> log.warn("워터마크 실패 전이 대상 없음. draftFileId={}", draftFileId));
	}

	// 워터마크 재처리 전이
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String markProcessingAndGetKey(Long draftFileId) {

		CommissionDraftFile file = commissionDraftFileRepository.findById(draftFileId)
			.orElseThrow(() -> new IllegalStateException("워터마크 재처리 대상 없음: " + draftFileId));

		file.retryWatermark();
		return file.getFileUrl();
	}

	// TODO: 디스코드 웹훅 - 영구 실패 알림 (draftFileId + 사유)
	private void notifyPermanentFailure(Long draftFileId) {

		log.error("워터마크 영구 실패. draftFileId={}", draftFileId);
	}
}
