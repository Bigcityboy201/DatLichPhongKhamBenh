package truonggg.sercurity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import truonggg.user.domain.model.User;
import truonggg.user.infrastructure.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

	private final UserRepository userRepository;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		logger.debug("Trying to load user: {}", username);
		
		User user = this.userRepository.findByUserName(username)
				.orElseThrow(() -> {
					logger.warn("User not found: {}", username);
					return new UsernameNotFoundException("Cannot find user with userName: " + username);
				});

		logger.debug("User found: {}, password hash: {}", user.getUserName(), user.getPassword());
		return new CustomUserDetails(user);
	}
}