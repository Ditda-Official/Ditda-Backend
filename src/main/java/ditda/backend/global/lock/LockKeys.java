package ditda.backend.global.lock;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LockKeys {

	/**
	 * 외주 매칭
	 * 지원(apply), 취소(cancel), 매칭 마감(process) 공유
	 */
	public static final String COMMISSION_MATCHING = "'commission:matching:' + #commissionId";
}
