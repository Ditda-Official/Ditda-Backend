package ditda.backend.domain.commission.category.textbook.dto.response;

import java.util.List;

import ditda.backend.domain.commission.core.dto.response.CategoryDetailResponse;
import ditda.backend.domain.commission.core.entity.enums.PageType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "교재 외지/내지 카테고리 상세")
public record TextbookDetailResponse(

	@Schema(description = "교재명", example = "수학의 정석")
	String textbookName,

	@Schema(description = "강사명", example = "홍길동")
	String instructorName,

	@Schema(description = "과목", example = "수학")
	String subject,

	@Schema(description = "필요 페이지 정보")
	List<RequiredPage> requiredPages

) implements CategoryDetailResponse {

	public record RequiredPage(

		@Schema(description = "페이지 종류", example = "INSTRUCTOR_PROFILE")
		PageType pageType,

		@Schema(description = "페이지 설명", example = "프로필은 깔끔하게")
		String description
	) {
	}
}
