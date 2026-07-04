package ditda.backend.domain.settlement.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementType {
	BASE("기본금"),
	FINAL("최종금액");

	private final String description;
}
