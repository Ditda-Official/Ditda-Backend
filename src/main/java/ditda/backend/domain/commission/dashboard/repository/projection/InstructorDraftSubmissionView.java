package ditda.backend.domain.commission.dashboard.repository.projection;

import ditda.backend.domain.commission.core.entity.Commission;

public interface InstructorDraftSubmissionView {

	Commission getCommission();

	Long getSubmissionCount();
}
