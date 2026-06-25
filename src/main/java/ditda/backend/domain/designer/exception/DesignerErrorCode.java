package ditda.backend.domain.designer.exception;

import org.springframework.http.HttpStatus;

import ditda.backend.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DesignerErrorCode implements BaseErrorCode {

	DESIGNER_NOT_FOUND(HttpStatus.NOT_FOUND, "DESIGNER_404_01", "존재하지 않는 디자이너입니다."),
	DESIGNER_LEVEL_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "DESIGNER_404_02", "존재하지 않는 디자이너 레벨 정책입니다." ),
	PORTFOLIO_FILE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "DESIGNER_400_01", "포트폴리오 파일은 최대 3개까지 업로드 가능합니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
