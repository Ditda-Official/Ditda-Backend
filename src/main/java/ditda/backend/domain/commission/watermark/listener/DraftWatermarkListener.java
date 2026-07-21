package ditda.backend.domain.commission.watermark.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import ditda.backend.domain.commission.draft.event.DraftFilesSubmittedEvent;
import ditda.backend.domain.commission.watermark.service.DraftWatermarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DraftWatermarkListener {

	private final DraftWatermarkService draftWatermarkService;

	@Async("watermarkExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onDraftFilesSubmitted(DraftFilesSubmittedEvent event) {
		try {
			draftWatermarkService.watermarkDraftFiles(event.draftId());
		} catch (Exception exception) {
			log.error("워터마크 파이프라인 실패. draftId={}", event.draftId(), exception);
		}
	}
}
