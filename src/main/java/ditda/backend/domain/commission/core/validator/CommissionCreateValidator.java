package ditda.backend.domain.commission.core.validator;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest;
import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest.ColorInfo;
import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest.DateInfo;
import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest.DesignInfo;
import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest.TermRequest;
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

		validateColors(request.designInfo());
		validateTerm(request.term());
		validateDeadlines(request.date());

		CommissionCategoryHandler handler = commissionCategoryHandlerResolver.resolve(request.category());
		handler.validate(request);
		return handler;
	}

	// 색상 검증 (직접 색상 지정시 MAIN, SUB1, SUB2 필수)
	private void validateColors(DesignInfo design) {

		if (design.colorSelectionMode() != ColorSelectionMode.USER_SELECTED) {
			return;
		}

		List<ColorInfo> colors = design.colors();
		// 색상이 아예 없을 경우
		if (colors == null || colors.isEmpty()) {
			throw new GeneralException(CommissionErrorCode.COLORS_REQUIRED);
		}

		Set<ColorRole> roles = colors.stream()
			.map(ColorInfo::role)
			.collect(Collectors.toSet());

		// MAIN, SUB1, SUB2 중 부분 누락 or 중복
		if (roles.size() != colors.size() || !roles.equals(EnumSet.allOf(ColorRole.class))) {
			throw new GeneralException(CommissionErrorCode.INVALID_COLOR_COMPOSITION);
		}
	}

	// 결제 약관 검증
	private void validateTerm(TermRequest term) {

		if (term.type() != TermType.SETTLEMENT || !Boolean.TRUE.equals(term.isAgreed())) {
			throw new GeneralException(CommissionErrorCode.SETTLEMENT_TERM_NOT_AGREED);
		}
	}

	// 마감 기한 검증
	private void validateDeadlines(DateInfo date) {

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
