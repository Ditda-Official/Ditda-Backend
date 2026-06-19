package ditda.backend.domain.commission.dashboard.repository.projection;

import ditda.backend.domain.commission.core.entity.Commission;

public interface RevisingView {

	Commission getCommission();

	Boolean getSubmitted();

	Boolean getHasUpdated();
}
