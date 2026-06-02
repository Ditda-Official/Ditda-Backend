package ditda.backend.domain.commission.core.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.core.dto.response.PlanListResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommissionService {

	// 외주 플랜 정보 조회
	@Transactional(readOnly = true)
	public PlanListResponse getPlans() {
		return PlanListResponse.from();
	}
}
