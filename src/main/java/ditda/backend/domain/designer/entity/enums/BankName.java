package ditda.backend.domain.designer.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BankName {

	TOSS("토스뱅크"),
	KAKAO("카카오뱅크"),
	KOOKMIN("국민은행"),
	IBK("기업은행"),
	NH("농협은행"),
	SHINHAN("신한은행"),
	IM("iM뱅크"),
	WOORI("우리은행"),
	CITI("한국시티은행"),
	HANA("하나은행"),
	SAVINGS("저축은행"),
	SAEMAUL("새마을금고"),
	SHINHYUP("신협"),
	POST_OFFICE("우체국");

	private final String koreanName;
}
