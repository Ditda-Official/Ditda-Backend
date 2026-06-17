package ditda.backend.domain.designer.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExpReward {

	PORTFOLIO(50),
	FIRST_DRAFT_SUBMIT(100),
	DRAFT_SELECTED(150),
	REVISION(200);

	private final int amount;
}
