package truonggg.service.IMPL;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import truonggg.Exception.NotFoundException;
import truonggg.Model.Role;
import truonggg.Model.User;
import truonggg.Model.UserRoles;
import truonggg.dto.reponseDTO.UserRolesResponseDTO;
import truonggg.dto.requestDTO.UserRolesDeleteRequestDTO;
import truonggg.dto.requestDTO.UserRolesRequestDTO;
import truonggg.dto.requestDTO.UserRolesUpdateRequestDTO;
import truonggg.mapper.UserRolesMapper;
import truonggg.repo.RoleRepository;
import truonggg.repo.UserRepository;
import truonggg.repo.UserRolesRepository;
import truonggg.service.UserRolesService;

@Service
@RequiredArgsConstructor
public class UserRolesServiceIMPL implements UserRolesService {

	private final UserRolesRepository userRolesRepository;
	private final UserRolesMapper userRolesMapper;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;

	@Override
	public UserRolesResponseDTO assignRole(UserRolesRequestDTO dto) {
		// Kiểm tra User tồn tại
		User user = this.userRepository.findById(dto.getUserId())
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		// Kiểm tra Role tồn tại
		Role role = this.roleRepository.findById(dto.getRoleId())
				.orElseThrow(() -> new NotFoundException("role", "Role Not Found"));

		// Kiểm tra User đã có Role này chưa
		Optional<UserRoles> existingUserRole = this.userRolesRepository.findByUserUserIdAndRoleRoleId(dto.getUserId(),
				dto.getRoleId());

		if (existingUserRole.isPresent()) {
			// Nếu đã có, cập nhật isActive
			UserRoles userRole = existingUserRole.get();
			userRole.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
			userRole = this.userRolesRepository.save(userRole);
			UserRolesResponseDTO response = this.userRolesMapper.toDTO(userRole);
			response.setIsActive(userRole.getIsActive());
			return response;
		}

		// Tạo mới UserRoles
		UserRoles userRole = this.userRolesMapper.toEntity(dto);
		userRole.setUser(user);
		userRole.setRole(role);
		userRole.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

		userRole = this.userRolesRepository.save(userRole);
		UserRolesResponseDTO response = this.userRolesMapper.toDTO(userRole);
		response.setIsActive(userRole.getIsActive());
		return response;
	}

	@Override
	public List<UserRolesResponseDTO> getAll() {
		List<UserRoles> userRoles = this.userRolesRepository.findAll();
		return userRoles.stream().map(userRole -> {
			UserRolesResponseDTO dto = this.userRolesMapper.toDTO(userRole);
			dto.setIsActive(userRole.getIsActive());
			return dto;
		}).toList();
	}

	@Override
	public List<UserRolesResponseDTO> getByUserId(Integer userId) {
		List<UserRoles> userRoles = this.userRolesRepository.findByUserUserId(userId);
		return userRoles.stream().map(userRole -> {
			UserRolesResponseDTO dto = this.userRolesMapper.toDTO(userRole);
			dto.setIsActive(userRole.getIsActive());
			return dto;
		}).toList();
	}

	@Override
	public List<UserRolesResponseDTO> getByRoleId(Integer roleId) {
		List<UserRoles> userRoles = this.userRolesRepository.findByRoleRoleId(roleId);
		return userRoles.stream().map(userRole -> {
			UserRolesResponseDTO dto = this.userRolesMapper.toDTO(userRole);
			dto.setIsActive(userRole.getIsActive());
			return dto;
		}).toList();
	}

	@Override
	public List<UserRolesResponseDTO> getActiveByUserId(Integer userId) {
		List<UserRoles> userRoles = this.userRolesRepository.findActiveByUserId(userId);
		return userRoles.stream().map(userRole -> {
			UserRolesResponseDTO dto = this.userRolesMapper.toDTO(userRole);
			dto.setIsActive(userRole.getIsActive());
			return dto;
		}).toList();
	}

	@Override
	public List<UserRolesResponseDTO> getActiveByRoleId(Integer roleId) {
		List<UserRoles> userRoles = this.userRolesRepository.findActiveByRoleId(roleId);
		return userRoles.stream().map(userRole -> {
			UserRolesResponseDTO dto = this.userRolesMapper.toDTO(userRole);
			dto.setIsActive(userRole.getIsActive());
			return dto;
		}).toList();
	}

	@Override
	public UserRolesResponseDTO findById(Integer id) {
		UserRoles userRole = this.userRolesRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("userRole", "UserRole Not Found"));
		UserRolesResponseDTO dto = this.userRolesMapper.toDTO(userRole);
		dto.setIsActive(userRole.getIsActive());
		return dto;
	}

	@Override
	public UserRolesResponseDTO update(UserRolesUpdateRequestDTO dto) {
		UserRoles foundUserRole = this.userRolesRepository.findById(dto.getUserRoleId())
				.orElseThrow(() -> new NotFoundException("userRole", "UserRole Not Found"));

		// Cập nhật User nếu có
		if (dto.getUserId() != null) {
			User user = this.userRepository.findById(dto.getUserId())
					.orElseThrow(() -> new NotFoundException("user", "User Not Found"));
			foundUserRole.setUser(user);
		}

		// Cập nhật Role nếu có
		if (dto.getRoleId() != null) {
			Role role = this.roleRepository.findById(dto.getRoleId())
					.orElseThrow(() -> new NotFoundException("role", "Role Not Found"));
			foundUserRole.setRole(role);
		}

		// Cập nhật isActive nếu có
		if (dto.getIsActive() != null) {
			foundUserRole.setIsActive(dto.getIsActive());
		}

		UserRoles savedUserRole = this.userRolesRepository.save(foundUserRole);
		UserRolesResponseDTO response = this.userRolesMapper.toDTO(savedUserRole);
		response.setIsActive(savedUserRole.getIsActive());
		return response;
	}

	@Override
	public boolean delete(UserRolesDeleteRequestDTO dto) {
		UserRoles foundUserRole = this.userRolesRepository.findById(dto.getUserRoleId())
				.orElseThrow(() -> new NotFoundException("userRole", "UserRole Not Found"));

		if (dto.getIsActive() != null) {
			foundUserRole.setIsActive(dto.getIsActive());
			this.userRolesRepository.save(foundUserRole);
		}
		return true;
	}

	@Override
	public boolean delete(Integer id) {
		UserRoles foundUserRole = this.userRolesRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("userRole", "UserRole Not Found"));

		this.userRolesRepository.delete(foundUserRole);
		return true;
	}

	@Override
	public boolean hasRole(Integer userId, Integer roleId) {
		return this.userRolesRepository.findByUserUserIdAndRoleRoleId(userId, roleId).isPresent();
	}

	@Override
	public boolean hasActiveRole(Integer userId, Integer roleId) {
		Optional<UserRoles> userRole = this.userRolesRepository.findByUserUserIdAndRoleRoleId(userId, roleId);
		return userRole.isPresent() && userRole.get().getIsActive();
	}
}
