package ditda.backend.domain.commission.dashboard.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import ditda.backend.domain.commission.application.service.CommissionApplicationService;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CommissionStatus;
import ditda.backend.domain.commission.core.entity.enums.PlanCode;
import ditda.backend.domain.commission.core.service.InstructorCommissionService;
import ditda.backend.domain.commission.dashboard.dto.response.MatchingCommissionResponse;

@ExtendWith(MockitoExtension.class)
class InstructorDashboardServiceTest {

	private static final Long INSTRUCTOR_ID = 100L;

	@Mock
	private InstructorCommissionService commissionService;

	@Mock
	private CommissionApplicationService commissionApplicationService;

	@InjectMocks
	private InstructorDashboardService instructorDashboardService;

	@Test
	@DisplayName("매칭 중인 외주별로 레벨 종류 수와 전체 지원자 수를 결합해 matched를 계산 - 성공")
	void getMatchingCommissions_matchedCal() {

		// given
		LocalDate deadline = LocalDate.of(2026, 6, 23);
		Commission max = Commission.builder()
			.id(1L)
			.title("MAX 외주")
			.planCode(PlanCode.MAX)
			.finalDeadline(deadline)
			.build();
		Commission basic = Commission.builder()
			.id(2L)
			.title("BASIC 외주")
			.planCode(PlanCode.BASIC)
			.finalDeadline(deadline)
			.build();

		List<Long> commissionIds = List.of(1L, 2L);

		given(commissionService.getCommissionByInstructorAndStatus(INSTRUCTOR_ID, CommissionStatus.RECRUITING))
			.willReturn(List.of(max, basic));
		given(commissionApplicationService.countDistinctLevelByStatus(commissionIds, ApplicationStatus.PENDING))
			.willReturn(Map.of(1L, 3L, 2L, 2L));    // max -> 레벨 3종류 / basic -> 레벨 2종류
		given(commissionApplicationService.countApplicationByStatus(commissionIds, ApplicationStatus.PENDING))
			.willReturn(Map.of(1L, 4L, 2L, 4L));    // max -> 지원자 4명 / basic -> 지원자 4명

		// when
		MatchingCommissionResponse response = instructorDashboardService.getMatchingCommissions(INSTRUCTOR_ID);

		// then
		assertThat(response.commissions()).hasSize(2);

		MatchingCommissionResponse.CommissionItem maxItem = response.commissions().getFirst();
		assertThat(maxItem.commissionId()).isEqualTo(1L);
		assertThat(maxItem.title()).isEqualTo("MAX 외주");
		assertThat(maxItem.finalDeadline()).isEqualTo(deadline);
		assertThat(maxItem.matching().matched()).isEqualTo(4);
		assertThat(maxItem.matching().total()).isEqualTo(5);

		MatchingCommissionResponse.CommissionItem basicItem = response.commissions().get(1);
		assertThat(basicItem.commissionId()).isEqualTo(2L);
		assertThat(basicItem.matching().matched()).isEqualTo(2);
		assertThat(basicItem.matching().total()).isEqualTo(3);
	}

	@Test
	@DisplayName("매칭 중인 외주가 없으면 빈 목록을 반환 - 성공")
	void getMatchingCommissions_emptyList() {

		// given
		given(commissionService.getCommissionByInstructorAndStatus(INSTRUCTOR_ID, CommissionStatus.RECRUITING))
			.willReturn(List.of());
		given(commissionApplicationService.countDistinctLevelByStatus(List.of(), ApplicationStatus.PENDING))
			.willReturn(Map.of());
		given(commissionApplicationService.countApplicationByStatus(List.of(), ApplicationStatus.PENDING))
			.willReturn(Map.of());

		// when
		MatchingCommissionResponse response = instructorDashboardService.getMatchingCommissions(INSTRUCTOR_ID);

		// then
		assertThat(response.commissions()).isEmpty();
	}
}
