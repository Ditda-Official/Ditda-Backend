package ditda.backend.domain.commission.dashboard.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ditda.backend.domain.commission.application.entity.enums.ApplicationStatus;
import ditda.backend.domain.commission.core.entity.Commission;
import ditda.backend.domain.commission.core.entity.enums.CommissionStatus;
import ditda.backend.domain.commission.core.entity.enums.PlanCode;
import ditda.backend.domain.commission.dashboard.dto.response.InstructorMatchingCommissionResponse;
import ditda.backend.domain.commission.dashboard.repository.DashboardCommissionRepository;
import ditda.backend.domain.commission.dashboard.repository.projection.InstructorMatchingView;

@ExtendWith(MockitoExtension.class)
class InstructorDashboardServiceTest {

	private static final Long INSTRUCTOR_ID = 100L;

	@Mock
	private DashboardCommissionRepository dashboardCommissionRepository;

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
			.applicationDeadline(deadline)
			.finalDeadline(deadline)
			.build();
		Commission basic = Commission.builder()
			.id(2L)
			.title("BASIC 외주")
			.planCode(PlanCode.BASIC)
			.applicationDeadline(deadline)
			.finalDeadline(deadline)
			.build();

		InstructorMatchingView maxView = mock(InstructorMatchingView.class);
		given(maxView.getCommission()).willReturn(max);
		given(maxView.getDistinctLevelCount()).willReturn(3L);   // 레벨 3종류
		given(maxView.getTotalCount()).willReturn(4L);           // 지원자 4명

		InstructorMatchingView basicView = mock(InstructorMatchingView.class);
		given(basicView.getCommission()).willReturn(basic);
		given(basicView.getDistinctLevelCount()).willReturn(2L); // 레벨 2종류
		given(basicView.getTotalCount()).willReturn(4L);         // 지원자 4명

		given(dashboardCommissionRepository.findInstructorMatchingViews(
			INSTRUCTOR_ID, CommissionStatus.RECRUITING, ApplicationStatus.PENDING
		)).willReturn(List.of(maxView, basicView));

		// when
		InstructorMatchingCommissionResponse response = instructorDashboardService.getMatchingCommissions(
			INSTRUCTOR_ID);

		// then
		assertThat(response.commissions()).hasSize(2);

		InstructorMatchingCommissionResponse.CommissionItem maxItem = response.commissions().getFirst();
		assertThat(maxItem.commissionId()).isEqualTo(1L);
		assertThat(maxItem.title()).isEqualTo("MAX 외주");
		assertThat(maxItem.applicationDeadline()).isEqualTo(deadline);
		assertThat(maxItem.matching().matched()).isEqualTo(4);
		assertThat(maxItem.matching().total()).isEqualTo(5);

		InstructorMatchingCommissionResponse.CommissionItem basicItem = response.commissions().get(1);
		assertThat(basicItem.commissionId()).isEqualTo(2L);
		assertThat(basicItem.matching().matched()).isEqualTo(2);
		assertThat(basicItem.matching().total()).isEqualTo(3);
	}

	@Test
	@DisplayName("매칭 중인 외주가 없으면 빈 목록을 반환 - 성공")
	void getMatchingCommissions_emptyList() {

		// given
		given(dashboardCommissionRepository.findInstructorMatchingViews(
			INSTRUCTOR_ID, CommissionStatus.RECRUITING, ApplicationStatus.PENDING
		)).willReturn(List.of());

		// when
		InstructorMatchingCommissionResponse response = instructorDashboardService.getMatchingCommissions(
			INSTRUCTOR_ID);

		// then
		assertThat(response.commissions()).isEmpty();
	}
}
