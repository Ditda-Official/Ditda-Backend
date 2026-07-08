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

	// 워터마크 실패 전이
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void fail(Long draftFileId) {
		commissionDraftFileRepository.findById(draftFileId)
			.ifPresentOrElse(CommissionDraftFile::markWatermarkFailed,
				() -> log.warn("워터마크 실패 전이 대상 없음. draftFileId={}", draftFileId)
			);
	}
}
