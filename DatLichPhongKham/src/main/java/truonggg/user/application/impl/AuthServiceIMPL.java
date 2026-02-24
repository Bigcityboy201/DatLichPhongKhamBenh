package truonggg.user.application.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import truonggg.Exception.AccountInactiveException;
import truonggg.Exception.NotFoundException;
import truonggg.Exception.UserAlreadyExistException;
import truonggg.role.domain.model.Role;
import truonggg.user.domain.model.User;
import truonggg.constant.SecurityRole;
import truonggg.dto.reponseDTO.SignInResponse;
import truonggg.dto.reponseDTO.UserResponseDTO;
import truonggg.dto.requestDTO.SignInRequest;
import truonggg.dto.requestDTO.SignUpRequest;
import truonggg.user.mapper.UserMapper;
import truonggg.role.infrastructure.RoleRepository;
import truonggg.sercurity.CustomUserDetails;
import truonggg.user.infrastructure.UserRepository;
import truonggg.user.application.AuthService;
import truonggg.user.application.PasswordService;
import truonggg.utils.JwtUtils;

@RequiredArgsConstructor
@Service
public class AuthServiceIMPL implements AuthService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordService passwordService;
	private final UserMapper userMapper;
	private final AuthenticationManager authenticationManager;
	private final JwtUtils jwtUtils;

	@Override
	@Transactional
	public UserResponseDTO signUp(SignUpRequest dto) {

		// nên thay gọi lỗi->tương ứng rằng mỗi feature sẽ là một lỗi nếu như này vi
		// phạm srp
		Map<String, String> errors = new HashMap<>();

		if (userRepository.existsByUserName(dto.getUserName())) {
			errors.put("userName", "UserName already existed!");
		}
		if (userRepository.existsByEmail(dto.getEmail())) {
			errors.put("email", "Email already existed!");
		}

		if (!errors.isEmpty()) {
			throw new UserAlreadyExistException(errors);
		}

		User user = userMapper.toModel(dto);
		user.setActive(false);
		user.setPassword(this.passwordService.encodePassword(user.getPassword()));

		// lấy role theo tên (USER/ADMIN)
		Role roleUser = this.roleRepository.findByRoleName(SecurityRole.ROLE_USER);
		if (roleUser == null) {
			throw new NotFoundException("role", "Default role USER not found");
		}

		// Gán role USER trực tiếp cho user
		user.setRole(roleUser);

		// lưu user
		user = userRepository.save(user);

		return userMapper.toDTO(user);
	}

	@Override
	public SignInResponse signIn(SignInRequest dto) {

		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(dto.getUserName(), dto.getPassword()));

		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

		User user = userRepository.findByUserName(userDetails.getUsername())
				.orElseThrow(() -> new NotFoundException("user", "User not found"));

		// isActive = false nghĩa là tài khoản đang hoạt động; true là chưa kích hoạt /
		// inactive
		if (Boolean.TRUE.equals(user.getIsActive())) {
			throw new AccountInactiveException();
		}

		String accessToken = jwtUtils.generateToken(userDetails);
		Date expiredDate = jwtUtils.extractExpiration(accessToken);

		return SignInResponse.builder().token(accessToken).expiredDate(expiredDate).user(userMapper.toDTO(user))
				.build();
	}

}


