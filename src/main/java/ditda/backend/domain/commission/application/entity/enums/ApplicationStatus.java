package ditda.backend.domain.commission.application.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationStatus {
	PENDING("지원 완료"),
	ASSIGNED("1차 시안 대상자"),
	APPLICATION_REJECTED("지원 탈락"),
	DRAFT_SUBMITTED("1차 시안 제출 완료"),
	DRAFT_MISSED("1차 시안 미제출"),
	DRAFT_SELECTED("최종 선택"),
	DRAFT_REJECTED("최종 탈락");

	private final String description;
}
