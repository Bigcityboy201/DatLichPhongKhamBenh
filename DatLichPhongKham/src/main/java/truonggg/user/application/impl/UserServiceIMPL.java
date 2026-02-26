package truonggg.user.application.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import truonggg.Enum.UpdateContext;
import truonggg.Exception.NotFoundException;
import truonggg.Exception.UserAlreadyExistException;
import truonggg.role.application.RoleAssignmentHandler;
import truonggg.role.domain.model.Role;
import truonggg.user.domain.model.User;
import truonggg.constant.SecurityRole;
import truonggg.dto.reponseDTO.UserResponseDTO;
import truonggg.dto.requestDTO.AssignRoleRequestDTO;
import truonggg.dto.requestDTO.UserRequestDTO;
import truonggg.dto.requestDTO.UserUpdateRequestDTO;
import truonggg.user.mapper.UserMapper;
import truonggg.role.infrastructure.RoleRepository;
import truonggg.user.infrastructure.UserRepository;
import truonggg.reponse.PagedResult;
import truonggg.user.application.PasswordService;
import truonggg.user.application.UserManagementService;
import truonggg.user.application.UserSelfService;

@Service
@RequiredArgsConstructor
public class UserServiceIMPL implements UserManagementService, UserSelfService {
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordService passwordService;
	private final RoleRepository roleRepository;
    // Spring inject tất cả handler vào đây
    private final List<RoleAssignmentHandler> handlers;

    // Map build sau khi inject xong
    private Map<String, RoleAssignmentHandler> handlerMap;


    @PostConstruct
    public void init() {
        this.handlerMap = handlers == null
                ? Map.of()
                : handlers.stream()
                .collect(Collectors.toMap(
                        RoleAssignmentHandler::supportedRole,
                        Function.identity()
                ));
    }

	// create-user(srp)
    @Override
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO dto) {

        validateUser(dto);

        User user = userMapper.toEntity(dto);

        // encode password vẫn ở service (vì là infrastructure concern)
        user.setPassword(passwordService.encodePassword(user.getPassword()));

        // lấy default role
        Role roleUser = roleRepository.findByRoleName(SecurityRole.ROLE_USER);
        if (roleUser == null) {
            throw new NotFoundException("role", "Default role USER not found");
        }

        // gọi behavior domain
        user.changeRole(roleUser);
        user.markCreated();
        user.deactivate();        // mặc định active
        user.ensureValidState();  // check invariant

        return userMapper.toDTO(userRepository.save(user));
    }

	private void validateUser(UserRequestDTO dto) {
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
	}
	// kết thúc create

	// assignRole
    //coupling:gọi repo,mapper,entity doctor
    @Transactional
    @Override
    public UserResponseDTO assignRole(AssignRoleRequestDTO dto) {

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new NotFoundException("user", "User Not Found"));

        Role newRole = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new NotFoundException("role", "Role Not Found"));

        String oldRoleName = user.getRole() != null
                ? user.getRole().getRoleName()
                : null;

        String newRoleName = newRole.getRoleName();

        if (oldRoleName != null && oldRoleName.equals(newRoleName)) {
            return userMapper.toDTO(user); // không đổi gì
        }

        // remove role cũ
        if (oldRoleName != null) {
            RoleAssignmentHandler oldHandler = handlerMap.get(oldRoleName);
            if (oldHandler != null) {
                oldHandler.onRemoved(user);
            }
        }

        // đổi role
        user.assignRole(newRole);

        // add role mới
        RoleAssignmentHandler newHandler = handlerMap.get(newRoleName);
        if (newHandler != null) {
            newHandler.onAssigned(user);
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

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user", "User Not Found"));

        validateEmailUniqueness(user, dto);

        user.updateByAdmin(dto);

        return userMapper.toDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateStatus(Integer id, Boolean isActive) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user", "User Not Found"));

        if (Boolean.TRUE.equals(isActive)) {
            user.activate();
        } else if (Boolean.FALSE.equals(isActive)) {
            user.deactivate();
        }

        return userMapper.toDTO(user);
    }

    @Transactional
	@Override
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
    public UserResponseDTO updateProfile(String userName,
                                         UserUpdateRequestDTO dto) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new NotFoundException("user", "User Not Found"));

        validateEmailUniqueness(user, dto);

        user.updateBySelf(dto);

        return userMapper.toDTO(user);
    }

    private void validateEmailUniqueness(User user,
                                         UserUpdateRequestDTO dto) {

        if (dto.getEmail() == null)
            return;

        boolean exists = userRepository
                .existsByEmailAndUserIdNot(dto.getEmail(),
                        user.getUserId());

        if (exists) {
            throw new UserAlreadyExistException(
                    Map.of("email", "Email already exists"));
        }
    }
}


