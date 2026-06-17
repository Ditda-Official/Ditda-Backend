package ditda.backend.domain.commission.application.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import ditda.backend.domain.commission.application.repository.CommissionApplicationRepository;
import ditda.backend.domain.commission.application.repository.projection.ApplicationSubmissionCount;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommissionApplicationService {

	private final CommissionApplicationRepository commissionApplicationRepository;

	// commission별 상태 카운트
	public Map<Long, Long> countApplicationByStatus(List<Long> commissionIds, ApplicationStatus status) {
		if (commissionIds.isEmpty()) {
			return Map.of();
		}

		// 집계 결과
		Map<Long, Long> counts = commissionApplicationRepository
			.countByCommissionIdsAndStatus(commissionIds, status).stream()
			.collect(Collectors.toMap(
				ApplicationSubmissionCount::getCommissionId,
				ApplicationSubmissionCount::getCount
			));

		// 0 보정
		return commissionIds.stream()
			.collect(Collectors.toMap(id -> id, id -> counts.getOrDefault(id, 0L)));
	}
}
