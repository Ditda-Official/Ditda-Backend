package ditda.backend.domain.payment.entity.enums;

public enum PaymentStatus {
	PENDING,		// 외주 신청 + 입금 확인 대기
	COMPLETED,		// 외주 신청 + 입금 확인
	REFUNDED,		// 환불
	FAILED			// 실패
}
