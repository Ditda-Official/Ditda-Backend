package ditda.backend.domain.commission.dashboard.repository.projection;

import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.designer.entity.enums.DesignerLevel;

public interface DesignerDraftSubmissionView {

	Commission getCommission();

	ApplicationStatus getApplicationStatus();

	DesignerLevel getDesignerLevel();
}
