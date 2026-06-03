package ditda.backend.domain.term.dto;

import ditda.backend.domain.term.entity.enums.TermType;

public record TermAgreement(
	TermType type,
	String version,
	boolean isAgreed
) {
}
