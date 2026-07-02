package ditda.backend.domain.commission.draft.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "1차 시안 제출 요청")
public record DraftSubmitRequest(

	@Schema(
		description = "presigned URL로 업로드한 S3 임시 key 목록",
		example = "[\"commission/draft/tmp/uuid1.png\", \"commission/draft/tmp/uuid2.png\"]"
	)
	@NotNull(message = "시안 파일 key 목록은 필수입니다.")
	@Size(min = 1, max = 9, message = "시안 파일은 1개 이상 9개 이하여야 합니다.")
	List<@NotBlank(message = "파일 key는 필수입니다.") String> keys
) {
}
