package truonggg.service.IMPL;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import truonggg.dto.requestDTO.UserRequestDTO;
import truonggg.dto.requestDTO.UserUpdateRequestDTO;
import truonggg.mapper.UserMapper;
import truonggg.repo.RoleRepository;
import truonggg.repo.UserRepository;
import truonggg.repo.UserRolesRepository;
import truonggg.reponse.PagedResult;
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
	public UserResponseDTO signUp(User user) {
		if (userRepository.existsByUserName(user.getUserName())) {
			throw new UserAlreadyExistException("User with userName: " + user.getUserName() + " already existed!");
		}

		if (userRepository.existsByEmail(user.getEmail())) {
			throw new UserAlreadyExistException("User with email: " + user.getEmail() + " already existed!");
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
		this.syncUserRole(user, roleUser);

		System.out
				.println("Successfully created user: " + user.getUserName() + " with role: " + roleUser.getRoleName());

		return this.userMapper.toDTO(user);
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
		user = this.userRepository.save(user);
		this.syncUserRole(user, role);

		return true;
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

		return PagedResult.<UserResponseDTO>builder().content(dtoList)
				.totalElements((int) userPage.getTotalElements()).totalPages(userPage.getTotalPages())
				.currentPage(userPage.getNumber()).pageSize(userPage.getSize()).build();
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

		// Cập nhật thông tin từ DTO
		user.setFullName(dto.getFullName());
		user.setEmail(dto.getEmail());
		user.setPhone(dto.getPhone());
		user.setAddress(dto.getAddress());
		if (dto.getDateOfBirth() != null)
			user.setDateOfBirth(dto.getDateOfBirth());
		if (dto.getIsActive() != null) {
			user.setActive(dto.getIsActive());
		}

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
	private void syncUserRole(User user, Role newRole) {
		List<UserRoles> userRoles = new ArrayList<>(this.userRolesRepository.findByUserUserId(user.getUserId()));
		boolean hasExistingRole = false;

		for (UserRoles userRole : userRoles) {
			boolean isTargetRole = userRole.getRole() != null
					&& userRole.getRole().getRoleId().equals(newRole.getRoleId());
			userRole.setIsActive(isTargetRole);
			if (isTargetRole) {
				hasExistingRole = true;
			}
		}

		if (!hasExistingRole) {
			UserRoles newUserRole = new UserRoles();
			newUserRole.setUser(user);
			newUserRole.setRole(newRole);
			newUserRole.setIsActive(true);
			userRoles.add(newUserRole);
		}

		this.userRolesRepository.saveAll(userRoles);
	}
}
