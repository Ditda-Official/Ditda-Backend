package ditda.backend.domain.commission.core.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CommissionStatus;
import ditda.backend.domain.commission.core.exception.CommissionErrorCode;
import ditda.backend.domain.commission.core.repository.CommissionRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DesignerCommissionService {

	private final CommissionRepository commissionRepository;

	// 모집 중 외주 목록 조회
	@Transactional(readOnly = true)
	public Page<Commission> getRecruitingCommissions(Pageable pageable) {
		return commissionRepository.findByStatusOrderByApplicationDeadlineAscIdAsc(
			CommissionStatus.RECRUITING,
			pageable
		);
	}

	@Transactional(readOnly = true)
	public Commission getSelectedCommission(Long commissionId, Long designerId) {

		Commission commission = commissionRepository.findById(commissionId)
			.orElseThrow(() -> new GeneralException(CommissionErrorCode.COMMISSION_NOT_FOUND));

		if (!commission.isSelectedBy(designerId)) {
			throw new GeneralException(CommissionErrorCode.COMMISSION_ACCESS_DENIED);
		}
		return commission;
	}
}
