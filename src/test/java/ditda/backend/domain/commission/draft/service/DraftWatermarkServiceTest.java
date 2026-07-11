package ditda.backend.domain.commission.draft.service;

import static org.mockito.BDDMockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import ditda.backend.global.image.WatermarkImageProcessor;
import ditda.backend.global.image.dto.WatermarkedImage;
import ditda.backend.global.s3.enums.BucketType;
import ditda.backend.global.s3.enums.S3ContentType;
import ditda.backend.global.s3.manager.S3FileManager;

@ExtendWith(MockitoExtension.class)
class DraftWatermarkServiceTest {

	private static final Long DRAFT_ID = 1L;

	@Mock
	private CommissionDraftFileRepository commissionDraftFileRepository;

	@Mock
	private WatermarkImageProcessor watermarkImageProcessor;

	@Mock
	private S3FileManager s3FileManager;

	@Mock
	private DraftWatermarkTransitionService draftWatermarkTransitionService;

	@InjectMocks
	private DraftWatermarkService draftWatermarkService;

	@Test
	@DisplayName("원본 key로 wm 경로를 파생해 업로드하고 COMPLETED로 전이 - 성공")
	void watermarkDraftFiles_success() throws IOException {

		// given
		CommissionDraftFile file = draftFile(10L, "commission/draft/abc.png");
		given(commissionDraftFileRepository.findAllByCommissionDraftIdAndWatermarkStatus(
			DRAFT_ID, WatermarkStatus.PROCESSING)).willReturn(List.of(file));
		given(s3FileManager.download(BucketType.PRIVATE, "commission/draft/abc.png"))
			.willReturn(new ByteArrayInputStream(new byte[0]));

		WatermarkedImage watermarked = new WatermarkedImage(new byte[]{1, 2, 3}, S3ContentType.PNG);
		given(watermarkImageProcessor.createWatermarkedPreview(any())).willReturn(watermarked);

		// when
		draftWatermarkService.watermarkDraftFiles(DRAFT_ID);

		// then
		then(s3FileManager).should().upload(
			BucketType.PRIVATE,
			"commission/draft/wm/abc.png",
			watermarked.bytes(),
			S3ContentType.PNG.getContentType()
		);
		then(draftWatermarkTransitionService).should().complete(10L, "commission/draft/wm/abc.png");
		then(draftWatermarkTransitionService).should(never()).fail(anyLong());
	}

	@Test
	@DisplayName("워터마크 생성 실패 시 업로드 없이 FAILED로 전이")
	void watermarkDraftFiles_markFailedOnError() throws IOException {

		// given
		CommissionDraftFile file = draftFile(10L, "commission/draft/abc.png");
		given(commissionDraftFileRepository.findAllByCommissionDraftIdAndWatermarkStatus(
			DRAFT_ID, WatermarkStatus.PROCESSING)).willReturn(List.of(file));
		given(s3FileManager.download(any(), anyString()))
			.willReturn(new ByteArrayInputStream(new byte[0]));
		given(watermarkImageProcessor.createWatermarkedPreview(any()))
			.willThrow(new IOException("이미지 처리 실패"));

		// when
		draftWatermarkService.watermarkDraftFiles(DRAFT_ID);

		// then
		then(s3FileManager).should(never()).upload(any(), anyString(), any(), anyString());
		then(draftWatermarkTransitionService).should().fail(10L);
		then(draftWatermarkTransitionService).should(never()).complete(anyLong(), anyString());
	}

	@Test
	@DisplayName("한 파일이 실패해도 나머지 파일은 계속 처리")
	void watermarkDraftFiles_isolatesFailurePerFile() throws IOException {

		// given
		CommissionDraftFile failing = draftFile(10L, "commission/draft/aaa.png");
		CommissionDraftFile succeeding = draftFile(20L, "commission/draft/bbb.png");
		given(commissionDraftFileRepository.findAllByCommissionDraftIdAndWatermarkStatus(
			DRAFT_ID, WatermarkStatus.PROCESSING)).willReturn(List.of(failing, succeeding));
		given(s3FileManager.download(any(), anyString()))
			.willAnswer(invocation -> new ByteArrayInputStream(new byte[0]));

		WatermarkedImage watermarked = new WatermarkedImage(new byte[]{1, 2, 3}, S3ContentType.PNG);
		given(watermarkImageProcessor.createWatermarkedPreview(any()))
			.willThrow(new IOException("첫 번째 파일 실패"))
			.willReturn(watermarked);

		// when
		draftWatermarkService.watermarkDraftFiles(DRAFT_ID);

		// then
		then(draftWatermarkTransitionService).should().fail(10L);
		then(draftWatermarkTransitionService).should().complete(20L, "commission/draft/wm/bbb.png");
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
		then(s3FileManager).shouldHaveNoInteractions();
		then(draftWatermarkTransitionService).shouldHaveNoInteractions();
	}

	private CommissionDraftFile draftFile(Long id, String fileUrl) {
		return CommissionDraftFile.builder()
			.id(id)
			.fileOrder(0)
			.fileUrl(fileUrl)
			.watermarkStatus(WatermarkStatus.PROCESSING)
			.build();
	}
}
