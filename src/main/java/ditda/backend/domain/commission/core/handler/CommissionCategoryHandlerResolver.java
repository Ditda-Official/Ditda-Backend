package ditda.backend.domain.commission.core.handler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.core.entity.enums.CategoryType;
import ditda.backend.domain.commission.core.exception.CommissionErrorCode;
import ditda.backend.global.apipayload.exception.GeneralException;

/**
 * 카테고리(CategoryType)에 맞는 CommissionCategoryHandler를 찾아준다.
 * 새 카테고리 추가 = 핸들러 구현체(@Component) 하나 추가 → 자동 등록
 */
@Component
public class CommissionCategoryHandlerResolver {

	private final Map<CategoryType, CommissionCategoryHandler> handlers;

	// CommissionCategoryHandler 구현체 전부 주입 -> 카테고리별 Map으로 변환
	public CommissionCategoryHandlerResolver(List<CommissionCategoryHandler> handlers) {
		this.handlers = handlers.stream()
			.collect(Collectors.toMap(CommissionCategoryHandler::category, handler -> handler));
	}

	// 카테고리로 Handler 조회 (미지원 카테고리면 예외처리)
	public CommissionCategoryHandler resolve(CategoryType categoryType) {

		CommissionCategoryHandler handler = handlers.get(categoryType);
		if (handler == null) {
			throw new GeneralException(CommissionErrorCode.UNSUPPORTED_CATEGORY);
		}
		return handler;
	}

}
