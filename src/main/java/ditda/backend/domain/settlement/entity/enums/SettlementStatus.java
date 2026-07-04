package ditda.backend.domain.settlement.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementStatus {

	PENDING("정산 대기"),
	COMPLETED("정산 완료"),
	FAILED("정산 실패");

	private final String description;
}
