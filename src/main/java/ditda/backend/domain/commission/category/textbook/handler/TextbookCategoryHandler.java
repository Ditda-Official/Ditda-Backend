package ditda.backend.domain.commission.category.textbook.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.category.textbook.dto.TextbookCategoryDetail;
import ditda.backend.domain.commission.category.textbook.entity.Textbook;
import ditda.backend.domain.commission.category.textbook.entity.TextbookPage;
import ditda.backend.domain.commission.category.textbook.repository.TextbookPageRepository;
import ditda.backend.domain.commission.category.textbook.repository.TextbookRepository;
import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest;
import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest.TextbookDetail;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CategoryType;
import ditda.backend.domain.commission.core.exception.CommissionErrorCode;
import ditda.backend.domain.commission.core.handler.CategoryDetail;
import ditda.backend.domain.commission.core.handler.CommissionCategoryHandler;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TextbookCategoryHandler implements CommissionCategoryHandler {

	private final TextbookRepository textbookRepository;
	private final TextbookPageRepository textbookPageRepository;

	@Override
	public CategoryType category() {
		return CategoryType.FLYER_TEXTBOOK_COVER_INNER;
	}

	@Override
	public void validate(CommissionCreateRequest request) {

		TextbookDetail detail = request.textbook();
		if (detail == null) {
			throw new GeneralException(CommissionErrorCode.TEXTBOOK_DETAIL_REQUIRED);
		}
	}

	@Override
	public String buildTitle(CommissionCreateRequest request) {

		TextbookDetail detail = request.textbook();
		return detail.textbookName() + " - " + detail.instructorName();
	}

	@Override
	public void saveDetail(Commission commission, CommissionCreateRequest request) {

		TextbookDetail detail = request.textbook();

		Textbook textbook = Textbook.create(
			commission,
			detail.textbookName(),
			detail.instructorName(),
			detail.subject()
		);
		textbookRepository.save(textbook);

		List<TextbookPage> pages = detail.requiredPages().stream()
			.map(p -> TextbookPage.create(
				commission,
				p.pageType(),
				p.description()
			))
			.toList();
		textbookPageRepository.saveAll(pages);
	}

	@Override
	public CategoryDetail loadDetail(Long commissionId) {

		Textbook textbook = textbookRepository.findById(commissionId)
			.orElseThrow(() -> new IllegalStateException("Textbook not found for commissionId=" + commissionId));

		List<TextbookPage> pages = textbookPageRepository.findByCommissionId(commissionId);

		return new TextbookCategoryDetail(textbook, pages);
	}
}
