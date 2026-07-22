package ditda.backend.domain.commission.draft.service;

import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ditda.backend.domain.commission.draft.entity.CommissionDraftFile;
import ditda.backend.domain.commission.draft.entity.enums.WatermarkStatus;
import ditda.backend.domain.commission.draft.repository.CommissionDraftFileRepository;
import ditda.backend.domain.commission.watermark.processor.WatermarkProcessor;
import ditda.backend.domain.commission.watermark.service.DraftWatermarkService;
import ditda.backend.domain.commission.watermark.service.DraftWatermarkTransitionService;

@ExtendWith(MockitoExtension.class)
class DraftWatermarkServiceTest {

	private static final Long DRAFT_ID = 1L;

	@Mock
	private CommissionDraftFileRepository commissionDraftFileRepository;

	@Mock
	private DraftWatermarkTransitionService draftWatermarkTransitionService;

	@Mock
	private WatermarkProcessor watermarkProcessor;

	@InjectMocks
	private DraftWatermarkService draftWatermarkService;

	@Test
	@DisplayName("PROCESSING 상태 파일들을 각각 워터마크 프로세서에 위임")
	void watermarkDraftFiles_delegatesEachFile() {

		// given
		CommissionDraftFile first = draftFile(10L, "commission/draft/aaa.png");
		CommissionDraftFile second = draftFile(20L, "commission/draft/bbb.png");
		given(commissionDraftFileRepository.findAllByCommissionDraftIdAndWatermarkStatus(
			DRAFT_ID, WatermarkStatus.PROCESSING)).willReturn(List.of(first, second));

		// when
		draftWatermarkService.watermarkDraftFiles(DRAFT_ID);

		// then
		then(watermarkProcessor).should().process(10L, "commission/draft/aaa.png");
		then(watermarkProcessor).should().process(20L, "commission/draft/bbb.png");
		then(watermarkProcessor).shouldHaveNoMoreInteractions();
	}

	@Test
	@DisplayName("PROCESSING 파일이 없으면 아무 작업도 하지 않음")
	void watermarkDraftFiles_noopWhenEmpty() {

		// given
		given(commissionDraftFileRepository.findAllByCommissionDraftIdAndWatermarkStatus(
			DRAFT_ID, WatermarkStatus.PROCESSING)).willReturn(List.of());

		// when
		draftWatermarkService.watermarkDraftFiles(DRAFT_ID);

		// then
		then(watermarkProcessor).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("재처리 시 원본 key를 조회해 워터마크 프로세서에 위임")
	void reprocessFile_delegatesWithOriginalKey() {

		// given
		given(draftWatermarkTransitionService.getOriginalKey(10L))
			.willReturn("commission/draft/abc.png");

		// when
		draftWatermarkService.reprocessFile(10L);

		// then
		then(watermarkProcessor).should().process(10L, "commission/draft/abc.png");
	}

	private CommissionDraftFile draftFile(Long id, String fileUrl) {
		return CommissionDraftFile.builder()
			.id(id)
			.fileOrder(0)
			.fileUrl(fileUrl)
			.watermarkStatus(WatermarkStatus.PROCESSING)
			.build();
	}

	@Test
	@DisplayName("프로세서가 예외를 던져도 실패 전이 후 나머지 파일 처리를 계속함")
	void watermarkDraftFiles_continuesWhenProcessorThrows() {

		// given
		CommissionDraftFile first = draftFile(10L, "commission/draft/aaa.png");
		CommissionDraftFile second = draftFile(20L, "commission/draft/bbb.png");
		given(commissionDraftFileRepository.findAllByCommissionDraftIdAndWatermarkStatus(
			DRAFT_ID, WatermarkStatus.PROCESSING)).willReturn(List.of(first, second));
		willThrow(new RuntimeException("unexpected")).given(watermarkProcessor)
			.process(10L, "commission/draft/aaa.png");

		// when
		draftWatermarkService.watermarkDraftFiles(DRAFT_ID);

		// then
		then(draftWatermarkTransitionService).should().fail(10L);
		then(watermarkProcessor).should().process(20L, "commission/draft/bbb.png");
	}
}
