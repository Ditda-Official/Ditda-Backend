package ditda.backend.domain.commission.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가격 정보")
public record PriceInfo(

	@Schema(description = "기본금", example = "60000")
	int baseAmount,

	@Schema(description = "최대 수령액", example = "210000")
	int maxAmount
) {
}
