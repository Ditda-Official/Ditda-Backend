package ditda.backend.domain.commission.core.validator;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest;
import ditda.backend.domain.commission.core.entity.enums.ColorRole;
import ditda.backend.domain.commission.core.entity.enums.ColorSelectionMode;
import ditda.backend.domain.commission.core.exception.CommissionErrorCode;
import ditda.backend.domain.commission.core.handler.CommissionCategoryHandler;
import ditda.backend.domain.commission.core.handler.CommissionCategoryHandlerResolver;
import ditda.backend.domain.term.entity.enums.TermType;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommissionCreateValidator {

	private static final int MIN_FIRST_DRAFT_LEAD_DAYS = 10;
	private static final int MIN_DRAFT_TO_FINAL_DAYS = 14;

	private final CommissionCategoryHandlerResolver commissionCategoryHandlerResolver;

	public CommissionCategoryHandler validate(CommissionCreateRequest request) {

		validateColors(request);
		validateTerm(request);
		validateDeadlines(request);

		CommissionCategoryHandler handler = commissionCategoryHandlerResolver.resolve(request.category());
		handler.validate(request);
		return handler;
	}

	// 색상 검증 (직접 색상 지정시 MAIN, SUB1, SUB2 필수)
	private void validateColors(CommissionCreateRequest request) {

		if (request.designInfo().colorSelectionMode() != ColorSelectionMode.USER_SELECTED) {
			return;
		}

		List<CommissionCreateRequest.ColorInfo> colors = request.designInfo().colors();
		if (colors == null || colors.isEmpty()) {
			throw new GeneralException(CommissionErrorCode.COLORS_REQUIRED);
		}

		Set<ColorRole> roles = colors.stream()
			.map(CommissionCreateRequest.ColorInfo::role)
			.collect(Collectors.toSet());

		if (roles.size() != colors.size() || !roles.equals(EnumSet.allOf(ColorRole.class))) {
			throw new GeneralException(CommissionErrorCode.COLORS_REQUIRED);
		}
	}

	// 결제 약관 검증
	private void validateTerm(CommissionCreateRequest request) {

		CommissionCreateRequest.TermRequest term = request.term();
		if (term.type() != TermType.SETTLEMENT || !Boolean.TRUE.equals(term.isAgreed())) {
			throw new GeneralException(CommissionErrorCode.SETTLEMENT_TERM_NOT_AGREED);
		}
	}

	// 마감 기한 검증
	private void validateDeadlines(CommissionCreateRequest request) {

		CommissionCreateRequest.DateInfo date = request.date();
		LocalDate today = LocalDate.now();

		if (!date.firstDraftDeadline().isBefore(date.finalDeadline())) {
			throw new GeneralException(CommissionErrorCode.INVALID_DEADLINE_ORDER);
		}

		if (date.firstDraftDeadline().minusDays(MIN_FIRST_DRAFT_LEAD_DAYS).isBefore(today)) {
			throw new GeneralException(CommissionErrorCode.FIRST_DRAFT_DEADLINE_TOO_SOON);
		}

		if (date.finalDeadline().isBefore(date.firstDraftDeadline().plusDays(MIN_DRAFT_TO_FINAL_DAYS))) {
			throw new GeneralException(CommissionErrorCode.FINAL_DEADLINE_TOO_SOON);
		}
	}
}
