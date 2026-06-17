package ditda.backend.domain.commission.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.commission.application.repository.CommissionApplicationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationService {

	private final CommissionApplicationRepository commissionApplicationRepository;

	@Transactional
	public void applySelection(Long commissionId, Long selectedApplicationId) {

		commissionApplicationRepository.findByCommission_Id(commissionId)
			.forEach(app -> {
				if (app.getId().equals(selectedApplicationId)) {
					app.markDraftSelected();
				} else {
					app.markDraftRejected();
				}
			});
	}
}
