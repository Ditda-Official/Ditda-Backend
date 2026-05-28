package ditda.backend.domain.common.commission.entity;

import ditda.backend.domain.common.commission.entity.enums.ConceptTag;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "commission_concepts")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommissionConcept {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "commission_concept_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "commission_id", nullable = false)
	private Commission commission;

	@Enumerated(EnumType.STRING)
	@Column(name = "concept", length = 30, nullable = false)
	private ConceptTag concept;

	public static CommissionConcept create(
		Commission commission,
		ConceptTag concept
	) {
		return CommissionConcept.builder()
			.commission(commission)
			.concept(concept)
			.build();
	}
}
