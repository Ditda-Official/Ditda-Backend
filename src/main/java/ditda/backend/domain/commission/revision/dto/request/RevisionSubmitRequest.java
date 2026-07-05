package ditda.backend.domain.commission.revision.dto.request;

import java.util.Comparator;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Schema(description = "수정본 제출 요청")
public record RevisionSubmitRequest(

	@Schema(description = "디자이너 코멘트", example = "요청하신 레이아웃/타이포 모두 반영했습니다.")
	@Size(max = 300)
	String designerComment,

	@Schema(description = "수정본 파일 목록")
	@NotEmpty
	@Valid
	List<RevisionFile> files
) {

	public record RevisionFile(

		@Schema(
			description = "presigned URL로 업로드한 S3 임시 key",
			example = "commission/draft/tmp/uuid1.png"
		)
		@NotBlank
		String key,

		@Schema(description = "파일 순서", example = "0")
		@Min(0)
		int fileOrder
	) {
	}

	public List<String> sortedKeys() {
		return files.stream()
			.sorted(Comparator.comparingInt(RevisionFile::fileOrder))
			.map(RevisionFile::key)
			.toList();
	}
}
