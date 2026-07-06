package ditda.backend.domain.commission.revision.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "수정본 제출 요청")
public record RevisionSubmitRequest(

	@Schema(description = "디자이너 코멘트", example = "요청하신 레이아웃/타이포 모두 반영했습니다.")
	@Size(max = 300)
	String designerComment,

	@Schema(
		description = "presigned URL로 업로드한 S3 임시 key 목록",
		example = "[\"commission/draft/tmp/uuid1.png\", \"commission/draft/tmp/uuid2.png\"]"
	)
	@NotNull(message = "시안 파일 key 목록은 필수입니다.")
	@Size(min = 1, max = 9, message = "시안 파일은 1개 이상 9개 이하여야 합니다.")
	List<@NotBlank(message = "파일 key는 필수입니다.") String> keys
) {
}
