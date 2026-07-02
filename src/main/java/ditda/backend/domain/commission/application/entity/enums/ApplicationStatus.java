package ditda.backend.domain.commission.application.entity.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationStatus {
	// Step 1 - 지원
	PENDING("지원 완료"),

	// Step 2 - 지원 결과
	ASSIGNED("1차 시안 대상자"),
	APPLICATION_REJECTED("지원 탈락"),

	// Step 3 - 1차 시안 제출 여부
	DRAFT_SUBMITTED("1차 시안 제출 완료"),
	DRAFT_MISSED("1차 시안 미제출"),

	// Step 4 - 최종 선택 여부
	DRAFT_SELECTED("최종 선택"),
	DRAFT_REJECTED("최종 탈락");

	private final String description;

	private static final Set<ApplicationStatus> DRAFT_SUBMITTED_STATUSES =
		EnumSet.of(DRAFT_SUBMITTED, DRAFT_SELECTED, DRAFT_REJECTED);

	public static Set<ApplicationStatus> draftSubmittedStatuses() {
		return Collections.unmodifiableSet(DRAFT_SUBMITTED_STATUSES);
	}
}
