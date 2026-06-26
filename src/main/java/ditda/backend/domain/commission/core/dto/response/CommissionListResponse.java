package ditda.backend.domain.commission.core.dto.response;

import java.time.LocalDate;
import java.util.List;

import ditda.backend.domain.commission.core.entity.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "디자이너 외주 목록 조회 응답")
public record CommissionListResponse(

	@Schema(description = "외주 목록")
	List<CommissionResponse> commissions,

	@Schema(description = "현재 페이지 번호", example = "0")
	int page,

	@Schema(description = "페이지 크기", example = "10")
	int size,

	@Schema(description = "전체 데이터 수", example = "23")
	long totalElements,

	@Schema(description = "전체 페이지 수", example = "3")
	int totalPages
) {

	@Schema(description = "외주 정보")
	public record CommissionResponse(

		@Schema(description = "외주 ID", example = "1")
		Long commissionId,

		@Schema(description = "지원 마감일", example = "2026-05-05")
		LocalDate applicationDeadline,

		@Schema(description = "외주 카테고리", example = "FLYER_TEXTBOOK_COVER_INNER")
		CategoryType category,

		@Schema(description = "제목", example = "해커스톡 왕초보 - 홍길동")
		String title,

		@Schema(description = "기본금 (원)", example = "40000")
		int baseAmount,

		@Schema(description = "최대금액 (원)", example = "210000")
		int maxAmount
	) {
	}
}
