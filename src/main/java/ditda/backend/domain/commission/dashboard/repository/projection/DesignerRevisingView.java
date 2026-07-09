package ditda.backend.domain.commission.dashboard.repository.projection;

import ditda.backend.domain.commission.core.entity.Commission;

public interface DesignerRevisingView {

	Commission getCommission();

	Boolean getSubmitted();

	Boolean getHasUpdated();
}
