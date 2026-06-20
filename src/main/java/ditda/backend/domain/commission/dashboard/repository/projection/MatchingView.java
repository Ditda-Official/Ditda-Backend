package ditda.backend.domain.commission.dashboard.repository.projection;

import ditda.backend.domain.commission.core.entity.Commission;

public interface MatchingView {

	Commission getCommission();

	Long getDistinctLevelCount();

	Long getTotalCount();
}
