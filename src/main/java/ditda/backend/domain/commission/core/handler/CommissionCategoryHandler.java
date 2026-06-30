package ditda.backend.domain.commission.core.handler;

import ditda.backend.domain.commission.core.dto.request.CommissionCreateRequest;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CategoryType;

public interface CommissionCategoryHandler {

	// 담당 카테고리
	CategoryType category();

	// 카테고리별 전용 검증
	void validate(CommissionCreateRequest request);

	// 카테고리별 전용 제목 생성
	String buildTitle(CommissionCreateRequest request);

	// 카테고리별 전용 저장
	void saveDetail(Commission commission, CommissionCreateRequest request);

	// 카테고리별 상세 조회
	CategoryDetail loadDetail(Long commissionId);
}
