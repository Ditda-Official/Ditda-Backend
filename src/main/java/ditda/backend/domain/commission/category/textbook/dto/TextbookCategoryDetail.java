package ditda.backend.domain.commission.category.textbook.dto;

import java.util.List;

import ditda.backend.domain.commission.category.textbook.dto.response.TextbookDetailResponse;
import ditda.backend.domain.commission.category.textbook.entity.Textbook;
import ditda.backend.domain.commission.category.textbook.entity.TextbookPage;
import ditda.backend.domain.commission.core.dto.response.CategoryDetailResponse;
import ditda.backend.domain.commission.core.handler.CategoryDetail;

public record TextbookCategoryDetail(
	Textbook textbook,
	List<TextbookPage> pages
) implements CategoryDetail {

	@Override
	public CategoryDetailResponse toResponse() {

		return new TextbookDetailResponse(
			textbook.getTitle(),
			textbook.getInstructorName(),
			textbook.getSubject(),
			pages.stream()
				.map(p -> new TextbookDetailResponse.RequiredPage(
					p.getPageType(), p.getPageDescription()))
				.toList()
		);
	}
}
