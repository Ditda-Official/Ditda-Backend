package ditda.backend.domain.designer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.domain.designer.exception.DesignerErrorCode;
import ditda.backend.domain.designer.repository.DesignerRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DesignerService {

	private final DesignerRepository designerRepository;

	@Transactional(readOnly = true)
	public Designer getById(Long designerId) {
		return designerRepository.findById(designerId)
			.orElseThrow(() -> new GeneralException(DesignerErrorCode.DESIGNER_NOT_FOUND));
	}
}
