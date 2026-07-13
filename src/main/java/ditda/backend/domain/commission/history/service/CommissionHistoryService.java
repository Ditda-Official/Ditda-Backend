package ditda.backend.domain.commission.history.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.history.repository.CommissionHistoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommissionHistoryService {

	private final CommissionHistoryRepository commissionHistoryRepository;

	// 강사 마이페이지 외주 내역 목록 조회
	@Transactional(readOnly = true)
	public Page<Commission> getInstructorCommissions(Long instructorId, Pageable pageable) {

		return commissionHistoryRepository.findByInstructorIdOrderByCreatedAtDescIdAsc(instructorId, pageable);
	}
}
