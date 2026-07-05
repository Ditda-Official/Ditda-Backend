package ditda.backend.domain.commission.application.policy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import ditda.backend.domain.commission.application.dto.SelectionResult;
import ditda.backend.domain.commission.application.entity.CommissionApplication;
import ditda.backend.domain.designer.entity.enums.DesignerLevel;

@Component
public class CommissionApplicationAssignmentPolicy {

	// 레벨별 1명 보장 후, 남은 슬롯은 선착순으로 채움
	public SelectionResult select(List<CommissionApplication> applications, int capacity) {

		// 지원시간 오름차순 정렬
		List<CommissionApplication> sorted = applications.stream()
			.sorted(Comparator.comparing(CommissionApplication::getCreatedAt))
			.toList();

		List<CommissionApplication> selected = new ArrayList<>();
		Set<Long> selectedIds = new HashSet<>();
		Set<DesignerLevel> levelGuaranteed = new HashSet<>();

		// 레벨별 1명 보장
		for (CommissionApplication app : sorted) {
			if (selected.size() >= capacity) {
				break;
			}

			DesignerLevel level = app.getDesigner().getLevel();
			if (!levelGuaranteed.contains(level)) {
				selected.add(app);
				selectedIds.add(app.getId());
				levelGuaranteed.add(level);
			}
		}

		// 남은 슬롯 선착순
		for (CommissionApplication app : sorted) {
			if (selected.size() == capacity) {
				break;
			}

			if (!selectedIds.contains(app.getId())) {
				selected.add(app);
				selectedIds.add(app.getId());
			}
		}

		// 탈락자
		List<CommissionApplication> rejected = sorted.stream()
			.filter(app -> !selectedIds.contains(app.getId()))
			.toList();

		return new SelectionResult(selected, rejected);
	}

}
