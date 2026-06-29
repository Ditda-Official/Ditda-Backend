package ditda.backend.domain.commission.core.dto;

import java.util.List;

import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.CommissionColor;
import ditda.backend.domain.commission.core.entity.CommissionConcept;
import ditda.backend.domain.commission.core.entity.CommissionFile;
import ditda.backend.domain.commission.core.handler.CategoryDetail;

public record CommissionDetail(
	Commission commission,
	List<CommissionConcept> concepts,
	List<CommissionColor> colors,
	List<CommissionFile> files,
	CategoryDetail categoryDetail
) {

}
