package ditda.backend.domain.designer.service;

import org.springframework.stereotype.Service;

import ditda.backend.domain.designer.entity.Designer;
import ditda.backend.domain.designer.exception.DesignerErrorCode;
import ditda.backend.domain.designer.repository.DesignerRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DesignerService {

	private final DesignerRepository designerRepository;

	public Designer findById(Long designerId) {
		return designerRepository.findById(designerId)
			.orElseThrow(() -> new GeneralException(DesignerErrorCode.DESIGNER_NOT_FOUND));
	}
}
