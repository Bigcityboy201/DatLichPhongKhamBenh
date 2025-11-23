package truonggg.service.IMPL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
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
import truonggg.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceIMPL implements UserService {
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
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
		user.setPassword(this.passwordEncoder.encode(user.getPassword()));

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
	public UserResponseDTO signUp(User user) {
		Map<String, String> errors = new HashMap<>();

		if (userRepository.existsByUserName(user.getUserName())) {
			errors.put("userName", "UserName already existed!");
		}
		if (userRepository.existsByEmail(user.getEmail())) {
			errors.put("email", "Email already existed!");
		}

		if (!errors.isEmpty()) {
			throw new UserAlreadyExistException(errors);
		}

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

		System.out
				.println("Successfully created user: " + user.getUserName() + " with role: " + roleUser.getRoleName());

		return this.userMapper.toDTO(user);
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
	public List<UserResponseDTO> getAll() {
		List<User> users = this.userRepository.findAll();
		return this.userMapper.toDTOList(users);
	}

	@Override
	public PagedResult<UserResponseDTO> getAllPaged(Pageable pageable) {
		Page<User> userPage = this.userRepository.findAll(pageable);
		List<UserResponseDTO> dtoList = userPage.stream().map(userMapper::toDTO).collect(Collectors.toList());

		return PagedResult.<UserResponseDTO>builder().content(dtoList).totalElements((int) userPage.getTotalElements())
				.totalPages(userPage.getTotalPages()).currentPage(userPage.getNumber()).pageSize(userPage.getSize())
				.build();
	}

	@Override
	public UserResponseDTO findById(Integer id) {
		User user = this.userRepository.findById(id).orElseThrow(() -> new NotFoundException("user", "User Not Found"));
		return this.userMapper.toDTO(user);
	}

	@Override
	@Transactional
	public User update(User user) {
		User foundUser = userRepository.findById(user.getUserId())
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		// Cập nhật từng field nếu không null
		if (user.getFullName() != null)
			foundUser.setFullName(user.getFullName());
		if (user.getEmail() != null)
			foundUser.setEmail(user.getEmail());
		if (user.getPhone() != null)
			foundUser.setPhone(user.getPhone());
		if (user.getAddress() != null)
			foundUser.setAddress(user.getAddress());
		if (user.getDateOfBirth() != null)
			foundUser.setDateOfBirth(user.getDateOfBirth());
		foundUser.setActive(user.getIsActive());

		return userRepository.save(foundUser);
	}

	@Override
	@Transactional
	public UserResponseDTO update(Integer id, UserUpdateRequestDTO dto) {
		User user = this.userRepository.findById(id).orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		// Update nếu DTO có giá trị (không null)
		if (dto.getFullName() != null)
			user.setFullName(dto.getFullName());

		if (dto.getEmail() != null)
			user.setEmail(dto.getEmail());

		if (dto.getPhone() != null)
			user.setPhone(dto.getPhone());

		if (dto.getAddress() != null)
			user.setAddress(dto.getAddress());

		if (dto.getDateOfBirth() != null)
			user.setDateOfBirth(dto.getDateOfBirth());

		if (dto.getIsActive() != null)
			user.setActive(dto.getIsActive());

		user = this.userRepository.save(user);
		return this.userMapper.toDTO(user);
	}

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
		User user = this.userRepository.findByUserName(userName)
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));
		if (dto.getFullName() != null)
			user.setFullName(dto.getFullName());
		if (dto.getEmail() != null)
			user.setEmail(dto.getEmail());
		if (dto.getPhone() != null)
			user.setPhone(dto.getPhone());
		if (dto.getAddress() != null)
			user.setAddress(dto.getAddress());
		if (dto.getDateOfBirth() != null)
			user.setDateOfBirth(dto.getDateOfBirth());

		user = this.userRepository.save(user);
		return this.userMapper.toDTO(user);
	}
}
