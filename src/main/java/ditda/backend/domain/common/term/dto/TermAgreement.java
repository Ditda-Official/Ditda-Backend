package ditda.backend.domain.common.term.dto;

import ditda.backend.domain.common.term.entity.enums.TermType;

public record TermAgreement(
	TermType type,
	String version,
	boolean isAgreed
) {
}
