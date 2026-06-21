package ditda.backend.domain.commission.core.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import ditda.backend.domain.commission.core.entity.enums.CategoryType;
import ditda.backend.domain.commission.core.entity.enums.ColorRole;
import ditda.backend.domain.commission.core.entity.enums.ColorSelectionMode;
import ditda.backend.domain.commission.core.entity.enums.ConceptTag;
import ditda.backend.domain.commission.core.entity.enums.FileKind;
import ditda.backend.domain.commission.core.entity.enums.PageSize;
import ditda.backend.domain.commission.core.entity.enums.PageType;
import ditda.backend.domain.commission.core.entity.enums.PlanCode;
import ditda.backend.domain.term.entity.enums.TermType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "새 외주 작성 요청")
public record CommissionCreateRequest(

	@Schema(description = "카테고리", example = "FLYER_TEXTBOOK_COVER_INNER")
	@NotNull(message = "카테고리는 필수입니다.")
	CategoryType category,

	@NotNull(message = "디자인 정보는 필수입니다.")
	@Valid
	DesignInfo designInfo,

	// 교재 내지/외지 카테고리 전용 (카테고리에 따라 비어있을 수 있어 @NotNull 제외 -> Handler에서 검증)
	@Schema(description = "교재 내지/외지 카테고리 상세")
	@Valid
	TextbookDetail textbook,

	@Schema(description = "첨부 파일 목록 (presign 업로드 후 받은 key)")
	@Valid
	List<@NotNull @Valid FileInfo> files,

	@Schema(description = "플랜", example = "PLUS")
	@NotNull(message = "플랜은 필수입니다.")
	PlanCode plan,

	@NotNull(message = "마감일 정보는 필수입니다.")
	@Valid
	DateInfo date,

	@NotNull(message = "결제 약관 동의는 필수입니다.")
	@Valid
	TermRequest term
) {

	@Schema(description = "디자인 정보")
	public record DesignInfo(

		@Schema(description = "사이즈", example = "A4")
		@NotNull(message = "사이즈는 필수입니다.")
		PageSize pageSize,

		@Schema(description = "컨셉 태그", example = "[\"CUTE\", \"ELEGANT\"]")
		@NotNull(message = "컨셉은 필수입니다.")
		@Size(min = 1, max = 2, message = "컨셉은 1개 이상 2개 이하여야 합니다.")
		List<@NotNull ConceptTag> concepts,

		@Schema(description = "추가 컨셉 설명", example = "초등 저학년 대상이라 너무 무겁지 않게")
		@Size(max = 300, message = "추가 컨셉 설명은 300자 이하여야 합니다.")
		String additionalConcept,

		@Schema(description = "색상 선택 모드", example = "USER_SELECTED")
		@NotNull(message = "색상 선택 모드는 필수입니다.")
		ColorSelectionMode colorSelectionMode,

		@Schema(description = "색상 목록 (USER_SELECTED일 때 필수)")
		@Valid
		List<@NotNull @Valid ColorInfo> colors
	) {
	}

	// 추후 카테고리 추가될 시, 다형 역직렬화(@JsonTypeInfo)를 통해 분리
	@Schema(description = "교재 외지/내지 카테고리 상세")
	public record TextbookDetail(

		@Schema(description = "교재명", example = "수학의 정석")
		@NotBlank(message = "교재명은 필수입니다.")
		@Size(max = 50, message = "교재명은 50자 이하여야 합니다.")
		String textbookName,

		@Schema(description = "강사명", example = "홍길동")
		@NotBlank(message = "강사명은 필수입니다.")
		@Size(max = 50, message = "강사명은 50자 이하여야 합니다.")
		String instructorName,

		@Schema(description = "과목", example = "수학")
		@NotBlank(message = "과목은 필수입니다.")
		@Size(max = 50, message = "과목은 50자 이하여야 합니다.")
		String subject,

		@NotEmpty(message = "외주 요청 페이지는 최소 1개 이상입니다.")
		@Valid
		List<@NotNull @Valid RequiredPage> requiredPages
	) {
	}

	@Schema(description = "색상 정보")
	public record ColorInfo(

		@Schema(description = "색상 역할", example = "MAIN")
		@NotNull(message = "색상 역할은 필수입니다.")
		ColorRole role,

		@Schema(description = "색상 코드(#RRGGBB)", example = "#194D33")
		@NotBlank(message = "색상 코드는 필수입니다.")
		@Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상 코드 형식이 올바르지 않습니다.")
		String colorCode
	) {
	}

	@Schema(description = "필요 페이지")
	public record RequiredPage(

		@Schema(description = "페이지 종류", example = "INSTRUCTOR_PROFILE")
		@NotNull(message = "페이지 종류는 필수입니다.")
		PageType pageType,

		@Schema(description = "페이지 설명", example = "프로필은 깔끔하게")
		@Size(max = 150, message = "페이지 설명은 150자 이하여야 합니다.")
		String description
	) {
	}

	@Schema(description = "첨부 파일")
	public record FileInfo(

		@Schema(description = "파일 종류", example = "MATERIAL")
		@NotNull(message = "파일 종류는 필수입니다.")
		FileKind fileKind,

		@Schema(description = "presigned URL로 업로드한 첨부파일 key 목록",
			example = "[\"commission/tmp/3f2b....png\", \"commission/tmp/9a1c....jpg\"]")
		@NotEmpty(message = "파일 key는 최소 1개 이상입니다.")
		List<@NotBlank(message = "파일 key는 비어 있을 수 없습니다.") String> keys,

		@Schema(description = "파일 설명", example = "img.04는 강사 프로필 이미지")
		@NotBlank(message = "파일 설명은 필수입니다.")
		@Size(max = 300, message = "파일 설명은 300자 이하여야 합니다.")
		String description
	) {
	}

	@Schema(description = "마감일 정보")
	public record DateInfo(

		@Schema(description = "1차 시안 마감일", example = "2026-06-15")
		@NotNull(message = "1차 시안 마감일은 필수입니다.")
		LocalDate firstDraftDeadline,

		@Schema(description = "최종 마감일", example = "2026-06-23")
		@NotNull(message = "최종 마감일은 필수입니다.")
		LocalDate finalDeadline
	) {
	}

	@Schema(description = "약관 동의")
	public record TermRequest(

		@Schema(description = "약관 종류", example = "SETTLEMENT")
		@NotNull(message = "약관 종류는 필수입니다.")
		TermType type,

		@Schema(description = "약관 버전", example = "V1.0")
		@NotBlank(message = "약관 버전은 필수입니다.")
		String version,

		@Schema(description = "약관 동의 여부", example = "true")
		@NotNull(message = "약관 동의 여부는 필수입니다.")
		@JsonProperty("isAgreed")
		Boolean isAgreed
	) {
	}
}
