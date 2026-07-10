package ditda.backend.global.notification;

public enum OutboxStatus {

	PENDING,        // 발송 대기
	SENT,            // 발송 완료
	FAILED            // 발송 실패
}
