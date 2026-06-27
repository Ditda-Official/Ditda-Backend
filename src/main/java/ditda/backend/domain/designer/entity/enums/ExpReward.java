package ditda.backend.domain.designer.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExpReward {

	PORTFOLIO(50),                    // 포트폴리오 확인 완료
	FIRST_DRAFT_SUBMIT(100),        // 1차 시안 제출하기
	DRAFT_SELECTED(150),            // 시안 선택되기
	COMMISSION_COMPLETED(200);        // 최종 확정되기

	private final int amount;
}
