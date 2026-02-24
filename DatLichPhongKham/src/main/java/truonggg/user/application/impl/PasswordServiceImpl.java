package truonggg.user.application.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import truonggg.user.application.PasswordService;

@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {
	private final PasswordEncoder passwordEncoder;

	@Override
	public String encodePassword(String rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}
}


