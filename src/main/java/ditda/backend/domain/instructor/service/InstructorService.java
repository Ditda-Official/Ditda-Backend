package ditda.backend.domain.instructor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.instructor.entity.Instructor;
import ditda.backend.domain.instructor.exception.InstructorErrorCode;
import ditda.backend.domain.instructor.repository.InstructorRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InstructorService {

	private final InstructorRepository instructorRepository;

	@Transactional(readOnly = true)
	public Instructor getById(Long instructorId) {

		return instructorRepository.findById(instructorId)
			.orElseThrow(() -> new GeneralException(InstructorErrorCode.INSTRUCTOR_NOT_FOUND));
	}

	@Transactional(readOnly = true)
	public Instructor getByIdWithUser(Long instructorId) {

		return instructorRepository.findByIdWithUser(instructorId)
			.orElseThrow(() -> new GeneralException(InstructorErrorCode.INSTRUCTOR_NOT_FOUND));
	}
}
