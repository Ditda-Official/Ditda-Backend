package ditda.backend.domain.commission.core.vo;

import java.util.List;

import ditda.backend.domain.commission.core.entity.enums.FileKind;

public record CommissionFileToSave(
	FileKind fileKind,
	List<String> keys,
	String description
) {
}
