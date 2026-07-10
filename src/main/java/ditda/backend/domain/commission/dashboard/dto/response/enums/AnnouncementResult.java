package ditda.backend.domain.commission.dashboard.dto.response.enums;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;

public enum AnnouncementResult {

	AWAITING,        // 대기중
	SELECTED,        // 선정됨
	NOT_SELECTED;    // 미선정

	private static final Map<ApplicationStatus, AnnouncementResult> MAPPING = Map.of(
		ApplicationStatus.PENDING, AWAITING,
		ApplicationStatus.ASSIGNED, SELECTED,
		ApplicationStatus.DRAFT_SUBMITTED, SELECTED,
		ApplicationStatus.DRAFT_MISSED, SELECTED,
		ApplicationStatus.DRAFT_SELECTED, SELECTED,
		ApplicationStatus.DRAFT_REJECTED, SELECTED,
		ApplicationStatus.APPLICATION_REJECTED, NOT_SELECTED
	);

	public static Set<ApplicationStatus> supportedStatuses() {
		return MAPPING.keySet();
	}

	// 특정 결과에 매핑되는 상태들
	public static Set<ApplicationStatus> statusesOf(AnnouncementResult result) {
		return MAPPING.entrySet().stream()
			.filter(e -> e.getValue() == result)
			.map(Map.Entry::getKey)
			.collect(Collectors.toUnmodifiableSet());
	}

	public static AnnouncementResult from(ApplicationStatus status) {
		AnnouncementResult result = MAPPING.get(status);
		if (result == null) {
			throw new IllegalStateException("발표 대기란에 올 수 없는 지원 상태: " + status);
		}

		return result;
	}

}
