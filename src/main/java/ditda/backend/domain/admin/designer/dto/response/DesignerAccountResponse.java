package ditda.backend.domain.admin.designer.dto.response;

import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.domain.designer.entity.enums.BankName;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "디자이너 계좌 정보 응답")
public record DesignerAccountResponse(

	@Schema(description = "디자이너 ID", example = "1")
	Long designerId,

	@Schema(description = "은행명", example = "TOSS")
	BankName bankName,

	@Schema(description = "계좌번호", example = "1234-5678-9012")
	String accountNumber,

	@Schema(description = "예금주", example = "홍길동")
	String accountHolder
) {

	public static DesignerAccountResponse from(Designer designer) {
		return new DesignerAccountResponse(
			designer.getId(),
			designer.getBankName(),
			designer.getAccountNumber(),
			designer.getAccountHolder()
		);
	}
}
