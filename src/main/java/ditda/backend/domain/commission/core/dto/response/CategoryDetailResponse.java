package ditda.backend.domain.commission.core.dto.response;

import ditda.backend.domain.commission.category.textbook.dto.response.TextbookDetailResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
	description = "카테고리별 상세 정보",
	oneOf = {TextbookDetailResponse.class}        // 카테고리 추가시 여기에 추가
)
public interface CategoryDetailResponse {
}
