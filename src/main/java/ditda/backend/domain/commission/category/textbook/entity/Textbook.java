package ditda.backend.domain.commission.category.textbook.entity;

import ditda.backend.domain.commission.core.entity.Commission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "textbooks")
@Getter
@Builder
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Textbook {

	@Id
	@Column(name = "commission_id")
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "commission_id")
	private Commission commission;

	@Column(name = "title", length = 50, nullable = false)
	private String title;

	@Column(name = "instructor_name", length = 50, nullable = false)
	private String instructorName;

	@Column(name = "subject", length = 50, nullable = false)
	private String subject;

	public static Textbook create(
		Commission commission,
		String title,
		String instructorName,
		String subject
	) {
		return Textbook.builder()
			.commission(commission)
			.title(title)
			.instructorName(instructorName)
			.subject(subject)
			.build();
	}
}
