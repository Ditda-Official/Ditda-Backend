package ditda.backend.domain.commission.revision.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.revision.dto.RevisionStatus;
import ditda.backend.domain.commission.revision.repository.RevisionRequestRepository;
import ditda.backend.domain.commission.revision.repository.projection.RevisionStatusView;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RevisionService {

	private final RevisionRequestRepository revisionRequestRepository;

	// 외주별 수정 상태(submitted/hasUpdated)
	public Map<Long, RevisionStatus> getRevisionStatuses(List<Long> commissionIds) {
		if (commissionIds.isEmpty()) {
			return Map.of();
		}

		Map<Long, RevisionStatus> statuses = revisionRequestRepository.findRevisionStatuses(commissionIds).stream()
			.collect(Collectors.toMap(
				RevisionStatusView::getCommissionId,
				this::toStatus
			));

		// 수정 요청이 없는 외주는 (false, false)
		commissionIds.forEach(id -> statuses.putIfAbsent(id, new RevisionStatus(false, false)));

		return statuses;
	}

	private RevisionStatus toStatus(RevisionStatusView view) {
		return new RevisionStatus(Boolean.TRUE.equals(view.getSubmitted()), Boolean.TRUE.equals(view.getHasUpdated()));
	}
}
