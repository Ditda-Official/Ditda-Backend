package ditda.backend.domain.commission.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.entity.CommissionApplication;
import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import ditda.backend.domain.commission.application.exception.ApplicationErrorCode;
import ditda.backend.domain.commission.application.repository.CommissionApplicationRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationService {

	private final CommissionApplicationRepository commissionApplicationRepository;

	@Transactional
	public void applySelection(List<CommissionApplication> applications, Long selectedApplicationId) {

		applications.forEach(app -> {
			if (app.getId().equals(selectedApplicationId)) {
				app.markDraftSelected();
			} else if (app.isDraftSubmitted()) {
				app.markDraftRejected();
			}
		});
	}

	// 모든 지원자를 디자이너/사용자 정보까지 fetch하여 조회
	@Transactional(readOnly = true)
	public List<CommissionApplication> getApplicantsWithDesignerAndUser(Long commissionId) {
		return commissionApplicationRepository.findWithDesignerAndUserByCommissionId(commissionId);
	}

	// 특정 외주에 대한 디자이너의 지원 조회
	@Transactional(readOnly = true)
	public CommissionApplication getApplicationByCommissionAndDesigner(Long commissionId, Long designerId) {
		return commissionApplicationRepository.findByCommissionAndDesigner(commissionId, designerId)
			.orElseThrow(() -> new GeneralException(ApplicationErrorCode.APPLICATION_NOT_FOUND));
	}

	// 특정 상태의 외주에 대한 지원자 수 조회
	@Transactional(readOnly = true)
	public long countByCommissionAndStatus(Long commissionId, ApplicationStatus status) {
		return commissionApplicationRepository.countByCommissionAndStatus(commissionId, status);
	}

	// 모든 지원자를 ASSIGNED로 전이
	@Transactional
	public void assignAll(List<CommissionApplication> applications) {
		applications.forEach(CommissionApplication::assign);
	}

	// 모든 지원자를 DRAFT_MISSED로 전이
	@Transactional
	public void markAllDraftMissed(List<CommissionApplication> applications) {
		applications.forEach(CommissionApplication::markDraftMissed);
	}

	// 모든 지원자를 APPLICATION_REJECTED로 전이
	@Transactional
	public void markAllApplicationRejected(List<CommissionApplication> applications) {
		applications.forEach(CommissionApplication::markApplicationRejected);
	}
}
