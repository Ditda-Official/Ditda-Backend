package ditda.backend.domain.commission.dashboard.repository.projection;

import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import ditda.backend.domain.commission.core.entity.Commission;

public interface DesignerAnnouncementView {

	Commission getCommission();

	ApplicationStatus getApplicationStatus();
}
