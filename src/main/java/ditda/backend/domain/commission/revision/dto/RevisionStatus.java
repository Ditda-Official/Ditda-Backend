package ditda.backend.domain.commission.revision.dto;

public record RevisionStatus(
	boolean submitted,
	boolean hasUpdated
) {
}
