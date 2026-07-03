package ditda.backend.domain.user.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ditda.backend.domain.user.entity.User;
import ditda.backend.domain.user.entity.enums.UserRole;
import ditda.backend.domain.user.exception.UserErrorCode;
import ditda.backend.domain.user.repository.UserRepository;
import ditda.backend.global.apipayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	public User findById(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(UserErrorCode.USER_NOT_FOUND));
	}

	public boolean existsByUsername(String username) {
		return userRepository.existsByUsername(username);
	}

	public Optional<User> findByUsernameIfExists(String username) {
		return userRepository.findByUsername(username);
	}

	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	public void validateUsernameAvailable(String username) {
		if (existsByUsername(username)) {
			throw new GeneralException(UserErrorCode.USERNAME_ALREADY_EXISTS);
		}
	}

	public void validateEmailAvailable(String email) {
		if (existsByEmail(email)) {
			throw new GeneralException(UserErrorCode.EMAIL_ALREADY_EXISTS);
		}
	}

	// 유저 생성
	@Transactional
	public User createUser(
		String username,
		String encodedPassword,
		String name,
		String email,
		String profileImage,
		String phone,
		UserRole role,
		LocalDateTime emailVerifiedAt
	) {
		User user = User.createUser(
			username,
			encodedPassword,
			name,
			email,
			profileImage,
			phone,
			role,
			emailVerifiedAt
		);

		return userRepository.save(user);
	}

}
