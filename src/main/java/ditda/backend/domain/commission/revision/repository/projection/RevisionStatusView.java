package ditda.backend.domain.commission.revision.repository.projection;

public interface RevisionStatusView {

	Long getCommissionId();

	Boolean getSubmitted();

	Boolean getHasUpdated();
}
