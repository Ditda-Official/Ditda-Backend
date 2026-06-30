package ditda.backend.domain.commission.revision.dto.request;

import java.util.List;

import ditda.backend.domain.commission.revision.entity.enums.RevisionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "시안 수정 요청")
public record RevisionCreateRequest(

	@NotNull(message = "수정 카테고리 정보는 필수입니다.")
	@Size(min = 1, max = 2, message = "수정 카테고리는 1개 이상 2개 이하여야 합니다.")
	@Valid
	List<RevisionCreateCategory> categories
) {

	@Schema(description = "수정 카테고리 정보")
	public record RevisionCreateCategory(

		@Schema(description = "수정 카테고리", example = "LAYOUT")
		@NotNull(message = "수정 카테고리는 필수입니다.")
		RevisionCategory category,

		@Schema(description = "수정 내용", example = "레이아웃을 더 깔끔하게 변경 부탁드립니다.")
		@NotBlank(message = "수정 내용은 필수입니다.")
		@Size(max = 300, message = "수정 내용은 300자 이하여야 합니다.")
		String comment
	) {
	}
}
