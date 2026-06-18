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
		List<ApplicationSubmissionCount> counts =
			commissionApplicationRepository.countByCommissionIdsAndStatus(commissionIds, status);

		return toCountMap(counts, commissionIds);
	}

	// commission별 지원자가 존재하는 디자이너 레벨 종류 수
	public Map<Long, Long> countDistinctLevelByStatus(List<Long> commissionIds, ApplicationStatus status) {
		if (commissionIds.isEmpty()) {
			return Map.of();
		}

		// 집계 결과
		List<ApplicationSubmissionCount> counts =
			commissionApplicationRepository.countDistinctLevelByCommissionIdsAndStatus(commissionIds, status);

		return toCountMap(counts, commissionIds);
	}

	// 집계 결과를 Map으로 변환 + 0 보정
	private Map<Long, Long> toCountMap(List<ApplicationSubmissionCount> counts, List<Long> commissionIds) {
		Map<Long, Long> countByCommissionId = counts.stream()
			.collect(Collectors.toMap(
				ApplicationSubmissionCount::getCommissionId,
				ApplicationSubmissionCount::getCount
			));

		return commissionIds.stream()
			.distinct()
			.collect(Collectors.toMap(id -> id, id -> countByCommissionId.getOrDefault(id, 0L)));
	}
}
