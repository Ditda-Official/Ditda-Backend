package ditda.backend.domain.commission.core.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import ditda.backend.domain.commission.core.entity.enums.PlanCode;

class CommissionTest {

	@DisplayName("플랜·레벨 분포별 매칭 확정 인원을 계산")
	@ParameterizedTest(name = "[{0}] 레벨종류 {1}, 지원자 {2} → matched {3}")
	@CsvSource({
		// plan, distinctLevels, totalApplicants, expected
		"BASIC, 2, 4, 2",   // L1:3/L2:1/L3:0
		"BASIC, 0, 0, 0",   // 지원자 없음
		"PLUS, 2, 4, 3",    // L1:3/L2:1/L3:0
		"PLUS, 3, 3, 3",    // 각 레벨 1명씩
		"MAX, 3, 4, 4",     // L1:2/L2:1/L3:1
		"MAX, 2, 4, 4",     // L1:3/L2:1/L3:0
		"MAX, 1, 5, 3",     // L1만 5명
		"MAX, 3, 5, 5"      // 전 레벨 + 정원
	})
	void matchedCount_boundaryValues(PlanCode plan, int distinctLevels, int totalApplicants, int expected) {

		Commission commission = Commission.builder()
			.planCode(plan)
			.build();

		assertThat(commission.matchedCount(distinctLevels, totalApplicants)).isEqualTo(expected);
	}
}
