package ditda.backend.domain.commission.core.dto.response;

import java.time.LocalDate;
import java.util.List;

import ditda.backend.domain.commission.core.dto.PriceInfo;
import ditda.backend.domain.commission.core.entity.enums.CategoryType;
import ditda.backend.domain.commission.core.entity.enums.ColorRole;
import ditda.backend.domain.commission.core.entity.enums.ColorSelectionMode;
import ditda.backend.domain.commission.core.entity.enums.CommissionStatus;
import ditda.backend.domain.commission.core.entity.enums.ConceptTag;
import ditda.backend.domain.commission.core.entity.enums.FileKind;
import ditda.backend.domain.commission.core.entity.enums.PageSize;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "외주 상세 정보 응답")
public record CommissionDetailResponse(

	@Schema(description = "외주 ID", example = "1")
	Long commissionId,

	@Schema(description = "제목", example = "수학의 정석 - 수학")
	String title,

	@Schema(description = "카테고리", example = "FLYER_TEXTBOOK_COVER_INNER")
	CategoryType category,

	@Schema(description = "상태", example = "PENDING")
	CommissionStatus status,

	@Schema(description = "디자인 정보")
	DesignInfo designInfo,

	@Schema(description = "카테고리별 상세 정보")
	CategoryDetailResponse categoryDetail,

	@Schema(description = "첨부 파일 목록")
	List<FileInfo> files,

	@Schema(description = "마감일 정보")
	DateInfo dateInfo,

	@Schema(description = "가격 정보")
	PriceInfo priceInfo
) {
	@Schema(description = "디자인 정보")
	public record DesignInfo(

		@Schema(description = "사이즈", example = "A4")
		PageSize pageSize,

		@Schema(description = "컨셉 정보")
		List<ConceptTag> concepts,

		@Schema(description = "추가 컨셉 설명", example = "초등 저학년 대상이라 너무 무겁지 않게")
		String additionalConcept,

		@Schema(description = "색상 선택 모드", example = "USER_SELECTED")
		ColorSelectionMode colorSelectionMode,

		@Schema(description = "색상 정보")
		List<ColorInfo> colors
	) {

		@Schema(description = "색상 정보")
		public record ColorInfo(

			@Schema(description = "색상 역할", example = "MAIN")
			ColorRole role,

			@Schema(description = "색상 코드(#RRGGBB)", example = "#194D33")
			String colorCode
		) {
		}
	}

	@Schema(description = "첨부 파일 정보")
	public record FileInfo(

		@Schema(description = "파일 종류", example = "MATERIAL")
		FileKind fileKind,

		@Schema(description = "첨부 파일 URL", example = "https://example.com/commission/file.pdf")
		List<String> fileUrls,

		@Schema(description = "파일 설명", example = "img.04는 강사 프로필 이미지")
		String description
	) {
	}

	@Schema(description = "마감일 정보")
	public record DateInfo(

		@Schema(description = "지원 마감일", example = "2026-06-08")
		LocalDate applicationDeadline,

		@Schema(description = "1차 시안 마감일", example = "2026-06-15")
		LocalDate firstDraftDeadline,

		@Schema(description = "최종 마감일", example = "2026-06-23")
		LocalDate finalDeadline
	) {
	}
}
