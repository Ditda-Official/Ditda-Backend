package ditda.backend.global.apipayload.response;

import java.util.List;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "페이지네이션 응답")
public record PageResponse<T>(

	@Schema(description = "목록")
	List<T> items,

	@Schema(description = "현재 페이지 번호", example = "0")
	int page,

	@Schema(description = "페이지 크기", example = "10")
	int size,

	@Schema(description = "전체 데이터 수", example = "23")
	long totalElements,

	@Schema(description = "전체 페이지 수", example = "3")
	int totalPages
) {

	public static <T> PageResponse<T> from(Page<T> page) {
		return new PageResponse<>(
			page.getContent(),
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages()
		);
	}
}
