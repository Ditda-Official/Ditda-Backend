package ditda.backend.global.apipayload.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "페이지네이션 요청")
public record PageQuery(

	@Schema(description = "페이지 번호", example = "0", defaultValue = "0")
	@Min(0) Integer page,

	@Schema(description = "페이지 크기", example = "10", defaultValue = "10")
	@Min(1) @Max(50) Integer size
) {

	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_SIZE = 10;

	public PageQuery {
		page = (page == null) ? DEFAULT_PAGE : page;
		size = (size == null) ? DEFAULT_SIZE : size;
	}

	public Pageable toPageable() {
		return PageRequest.of(page, size);
	}
}
