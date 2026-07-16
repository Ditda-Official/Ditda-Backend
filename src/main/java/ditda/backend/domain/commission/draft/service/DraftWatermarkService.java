package ditda.backend.domain.commission.draft.service;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.domain.commission.draft.entity.enums.WatermarkStatus;
import ditda.backend.domain.commission.draft.processor.WatermarkProcessor;
import ditda.backend.domain.commission.draft.repository.CommissionDraftFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DraftWatermarkService {

	private final CommissionDraftFileRepository commissionDraftFileRepository;
	private final DraftWatermarkTransitionService draftWatermarkTransitionService;
	private final WatermarkProcessor watermarkProcessor;

	public void watermarkDraftFiles(Long draftId) {

		List<CommissionDraftFile> files =
			commissionDraftFileRepository.findAllByCommissionDraftIdAndWatermarkStatus(
				draftId,
				WatermarkStatus.PROCESSING
			);

		files.forEach(f -> watermarkProcessor.process(f.getId(), f.getFileUrl()));
	}

	// 워터마크 재처리
	@Async("watermarkExecutor")
	public void reprocessFile(Long draftFileId) {

		String originalKey = draftWatermarkTransitionService.getOriginalKey(draftFileId);
		watermarkProcessor.process(draftFileId, originalKey);
	}
}
