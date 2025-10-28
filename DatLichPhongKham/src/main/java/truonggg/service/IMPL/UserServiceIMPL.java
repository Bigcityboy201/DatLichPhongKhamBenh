package truonggg.service.IMPL;

import java.sql.Date;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import truonggg.Exception.NotFoundException;
import truonggg.Exception.UserAlreadyExistException;
import truonggg.Model.Role;
import truonggg.Model.User;
import truonggg.Model.UserRoles;
import truonggg.constant.SecurityRole;
import truonggg.dto.reponseDTO.UserResponseDTO;
import truonggg.dto.requestDTO.AssignRoleRequestDTO;
import truonggg.dto.requestDTO.UserDeleteRequestDTO;
import truonggg.dto.requestDTO.UserRequestDTO;
import truonggg.dto.requestDTO.UserUpdateRequestDTO;
import truonggg.mapper.UserMapper;
import truonggg.repo.RoleRepository;
import truonggg.repo.UserRepository;
import truonggg.repo.UserRolesRepository;
import truonggg.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceIMPL implements UserService {
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	private final RoleRepository roleRepository;
	private final UserRolesRepository userRolesRepository;

	@Override
	public UserResponseDTO createUser(UserRequestDTO dto) {
		if (userRepository.existsByEmail(dto.getEmail())) {
			throw new NotFoundException("user", "Email already exists");
		}
		User user = userMapper.toEntity(dto);
		user = this.userRepository.save(user);
		UserResponseDTO userResponseDTO = this.userMapper.toDTO(user);
		return userResponseDTO;
	}

	@Override
	@Transactional
	public Boolean signUp(User user) {
		this.userRepository.findByUserName(user.getUserName()).ifPresent((u) -> {
			throw new UserAlreadyExistException("User with userName: %s already existed!".formatted(u.getUserName()));
		});

		user.setActive(false);
		user.setPassword(this.passwordEncoder.encode(user.getPassword()));

		// lấy role theo tên (USER/ADMIN)
		Role roleUser = this.roleRepository.findByRoleName(SecurityRole.ROLE_USER);
		if (roleUser == null) {
			throw new NotFoundException("role", "Default role USER not found");
		}

		// Gán role USER trực tiếp cho user
		user.setRole(roleUser);

		// lưu user
		user = this.userRepository.save(user);

		System.out.println("Successfully created user: " + user.getUserName() + " with role: " + roleUser.getRoleName());

		return true;
	}

	@Override
	@Transactional
	public Boolean assignRole(AssignRoleRequestDTO dto) {
		// Kiểm tra user tồn tại
		User user = this.userRepository.findById(dto.getUserId())
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		// Kiểm tra role tồn tại
		Role role = this.roleRepository.findById(dto.getRoleId())
				.orElseThrow(() -> new NotFoundException("role", "Role Not Found"));

		// Assign role mới (replace role cũ)
		user.setRole(role);
		this.userRepository.save(user);
		
		return true;
	}

	@Override
	public User update(User user) {
		// Tìm user hiện tại
		User foundUser = this.userRepository.findById(user.getUserId())
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		// Cập nhật thông tin user
		foundUser.setFullName(user.getFullName());
		foundUser.setEmail(user.getEmail());
		foundUser.setPhone(user.getPhone());
		foundUser.setAddress(user.getAddress());
		foundUser.setDateOfBirth(user.getDateOfBirth());
		foundUser.setActive(user.isActive());
		return this.userRepository.save(foundUser);
	}

	@Override
	public List<UserResponseDTO> getAll() {
		List<User> users = this.userRepository.findAll();
		return this.userMapper.toDTOList(users);
	}

	@Override
	public UserResponseDTO findById(Integer id) {
		User user = this.userRepository.findById(id).orElseThrow(() -> new NotFoundException("user", "User Not Found"));
		return this.userMapper.toDTO(user);
	}

	@Override
	@Transactional
	public UserResponseDTO update(UserUpdateRequestDTO dto) {
		User user = this.userRepository.findById(dto.getUserId())
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		// Cập nhật thông tin từ DTO
		user.setFullName(dto.getFullName());
		user.setEmail(dto.getEmail());
		user.setPhone(dto.getPhone());
		user.setAddress(dto.getAddress());
		if (dto.getDateOfBirth() != null && !dto.getDateOfBirth().isEmpty()) {
			user.setDateOfBirth(Date.valueOf(dto.getDateOfBirth()));
		}
		if (dto.getIsActive() != null) {
			user.setActive(dto.getIsActive());
		}

		user = this.userRepository.save(user);
		return this.userMapper.toDTO(user);
	}

	@Override
	@Transactional
	public boolean delete(UserDeleteRequestDTO dto) {
		User user = this.userRepository.findById(dto.getUserId())
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		// Soft delete - set isActive to false
		user.setActive(false);
		this.userRepository.save(user);
		return true;
	}

	@Override
	@Transactional
	public boolean delete(Integer id) {
		User user = this.userRepository.findById(id).orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		// Hard delete - remove from database
		this.userRepository.delete(user);
		return true;
	}
}
