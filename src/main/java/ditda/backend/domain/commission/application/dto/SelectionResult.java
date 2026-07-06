package ditda.backend.domain.commission.application.dto;

import java.util.List;

import ditda.backend.domain.commission.application.entity.CommissionApplication;

public record SelectionResult(
	List<CommissionApplication> selected,
	List<CommissionApplication> rejected
) {
}
