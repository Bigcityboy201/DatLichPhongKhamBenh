package truonggg.service.user.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import truonggg.Enum.UpdateContext;
import truonggg.Exception.NotFoundException;
import truonggg.Exception.UserAlreadyExistException;
import truonggg.Model.Doctors;
import truonggg.Model.Role;
import truonggg.Model.User;
import truonggg.constant.SecurityRole;
import truonggg.dto.reponseDTO.UserResponseDTO;
import truonggg.dto.requestDTO.AssignRoleRequestDTO;
import truonggg.dto.requestDTO.UserRequestDTO;
import truonggg.dto.requestDTO.UserUpdateRequestDTO;
import truonggg.mapper.UserMapper;
import truonggg.repo.DoctorsRepository;
import truonggg.repo.RoleRepository;
import truonggg.repo.UserRepository;
import truonggg.reponse.PagedResult;
import truonggg.service.user.PasswordService;
import truonggg.service.user.UserManagementService;
import truonggg.service.user.UserSelfService;

@Service
@RequiredArgsConstructor
public class UserServiceIMPL implements UserManagementService, UserSelfService {
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordService passwordService;
	private final RoleRepository roleRepository;
	private final DoctorsRepository doctorsRepository;

	@Override
	@Transactional
	public UserResponseDTO createUser(UserRequestDTO dto) {
		Map<String, String> errors = new HashMap<>();

		if (userRepository.existsByEmail(dto.getEmail())) {
			errors.put("email", "Email already exists");
		}

		if (userRepository.existsByUserName(dto.getUserName())) {
			errors.put("userName", "Username already exists");
		}

		if (!errors.isEmpty()) {
			throw new UserAlreadyExistException(errors);
		}

		User user = userMapper.toEntity(dto);

		// Encode password trước khi lưu
		user.setPassword(this.passwordService.encodePassword(user.getPassword()));

		// Set role mặc định là USER nếu chưa có
		if (user.getRole() == null) {
			Role roleUser = this.roleRepository.findByRoleName(SecurityRole.ROLE_USER);
			if (roleUser == null) {
				throw new NotFoundException("role", "Default role USER not found");
			}
			user.setRole(roleUser);
		}

		// Set isActive mặc định là false (cần admin kích hoạt)
		user.setActive(false);

		user = userRepository.save(user);
		return userMapper.toDTO(user);
	}

	@Override
	@Transactional
	public UserResponseDTO assignRole(AssignRoleRequestDTO dto) {
		// Lấy user
		User user = userRepository.findById(dto.getUserId())
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		// Lấy role
		Role role = roleRepository.findById(dto.getRoleId())
				.orElseThrow(() -> new NotFoundException("role", "Role Not Found"));

		// Gán role mới
		user.setRole(role);
		user = userRepository.save(user);

		final User finalUser = user;

		if (role.getRoleName().equalsIgnoreCase("DOCTOR")) {
			doctorsRepository.findByUser(finalUser).orElseGet(() -> {
				Doctors doctor = new Doctors();
				doctor.setUser(finalUser); // dùng finalUser
				doctor.setDescription(null);
				doctor.setExperienceYears(0);
				doctor.setIsActive(false);
				doctor.setDepartments(null);
				doctor.setIsFeatured(false);
				doctor.setImageUrl(null);
				return doctorsRepository.save(doctor);
			});
		}

		return userMapper.toDTO(user);
	}

	@Override
	public PagedResult<UserResponseDTO> getAllPaged(Pageable pageable) {
		Page<User> userPage = this.userRepository.findAll(pageable);
		List<UserResponseDTO> dtoList = userPage.stream().map(userMapper::toDTO).collect(Collectors.toList());

		return PagedResult.from(userPage, dtoList);
	}

	@Override
	public UserResponseDTO findById(Integer id) {
		User user = this.userRepository.findById(id).orElseThrow(() -> new NotFoundException("user", "User Not Found"));
		return this.userMapper.toDTO(user);
	}

	@Override
	@Transactional
	public UserResponseDTO update(Integer id, UserUpdateRequestDTO dto) {
		User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		validateAdminUpdate(user, dto);

		applyUserUpdate(user, dto, UpdateContext.ADMIN);

		return userMapper.toDTO(userRepository.save(user));
	}

	@Override
	@Transactional
	public UserResponseDTO updateStatus(Integer id, Boolean isActive) {
		User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		if (isActive != null) {
			user.setActive(isActive);
			userRepository.save(user);
		}
		return this.userMapper.toDTO(user);
	}

	@Override
	@Transactional
	public boolean deleteManually(Integer id) {
		User user = this.userRepository.findById(id).orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		// Hard delete - remove from database
		this.userRepository.delete(user);
		return true;
	}

	@Override
	public UserResponseDTO findByUserName(String userName) {
		User user = this.userRepository.findByUserName(userName)
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));
		return this.userMapper.toDTO(user);
	}

	@Override
	@Transactional
	public UserResponseDTO updateProfile(String userName, UserUpdateRequestDTO dto) {

		User user = userRepository.findByUserName(userName)
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		validateUpdateProfile(user, dto); // dùng Map ở đây

		applyUserUpdate(user, dto, UpdateContext.SELF);

		return userMapper.toDTO(userRepository.save(user));
	}

	private void validateUpdateProfile(User user, UserUpdateRequestDTO dto) {

		Map<String, String> errors = new HashMap<>();

		if (dto.getEmail() != null && userRepository.existsByEmailAndUserIdNot(dto.getEmail(), user.getUserId())) {

			errors.put("email", "Email already exists");
		}

		if (!errors.isEmpty()) {
			throw new UserAlreadyExistException(errors);
		}
	}

	private void validateAdminUpdate(User user, UserUpdateRequestDTO dto) {

		if (dto.getEmail() != null && userRepository.existsByEmailAndUserIdNot(dto.getEmail(), user.getUserId())) {

			throw new UserAlreadyExistException(Map.of("email", "Email already exists"));
		}
	}

	void applyUserUpdate(User user, UserUpdateRequestDTO dto, UpdateContext context) {

		if (dto.getFullName() != null)
			user.setFullName(dto.getFullName());

		if (dto.getEmail() != null) {

			user.setEmail(dto.getEmail());
		}

		if (dto.getPhone() != null)
			user.setPhone(dto.getPhone());

		if (dto.getAddress() != null)
			user.setAddress(dto.getAddress());

		if (dto.getDateOfBirth() != null)
			user.setDateOfBirth(dto.getDateOfBirth());

		// Trạng thái isActive được cập nhật qua API riêng (UserStatusDTO)
	}

}