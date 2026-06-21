package ditda.backend.domain.commission.dashboard.repository.projection;

import ditda.backend.domain.commission.core.entity.Commission;

public interface DraftSubmissionView {

	Commission getCommission();

	Long getSubmissionCount();
}
