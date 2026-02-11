package truonggg.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
import truonggg.service.user.impl.UserServiceIMPL;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
	@Mock
	private UserRepository userRepository;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private DoctorsRepository doctorsRepository;

	@Mock
	private UserMapper userMapper;

	@Mock
	private PasswordService passwordService;

	@InjectMocks
	private UserServiceIMPL userService;

	// test CreateUser
	@Test
	@DisplayName("createUser: success when email & username do not exist")
	void createUser_shouldReturnUserResponse_WhenUserDoesNotExist() {
		// ===== b1: chuẩn bị dữ liệu =====
		UserRequestDTO dto = new UserRequestDTO();
		dto.setEmail("test@gmail.com");
		dto.setUserName("testuser");
		dto.setPassword("123456");

		User userEntity = new User();
		userEntity.setPassword("123456"); // password thô trước khi encode
		userEntity.setRole(null); // để đi vào nhánh set default role

		Role roleUser = new Role();
		roleUser.setRoleName(SecurityRole.ROLE_USER);

		User savedUser = new User();
		savedUser.setUserId(1);
		savedUser.setEmail(dto.getEmail());
		savedUser.setUserName(dto.getUserName());
		savedUser.setRole(roleUser);
		savedUser.setActive(false);

		UserResponseDTO responseDTO = new UserResponseDTO();
		responseDTO.setUserId(1);
		responseDTO.setEmail(dto.getEmail());
		responseDTO.setUserName(dto.getUserName());

		// ===== b2: mock hành vi dependency =====
		when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
		when(userRepository.existsByUserName(dto.getUserName())).thenReturn(false);

		when(userMapper.toEntity(dto)).thenReturn(userEntity);
		when(passwordService.encodePassword(any())).thenReturn("encoded-password");

		when(roleRepository.findByRoleName(SecurityRole.ROLE_USER)).thenReturn(roleUser);

		when(userRepository.save(any(User.class))).thenReturn(savedUser);
		when(userMapper.toDTO(savedUser)).thenReturn(responseDTO);

		// ===== b3: gọi method cần test =====
		UserResponseDTO result = userService.createUser(dto);

		// ===== b4: assert kết quả =====
		assertNotNull(result);
		assertEquals(dto.getEmail(), result.getEmail());
		assertEquals(dto.getUserName(), result.getUserName());

		// ===== b5: verify hành vi quan trọng =====
		verify(userRepository).existsByEmail(dto.getEmail());
		verify(userRepository).existsByUserName(dto.getUserName());
		verify(passwordService).encodePassword(any());
		verify(userRepository).save(any(User.class));
	}

	@DisplayName("Create user - Throw exception when email already exists")
	@Test
	void createUser_ShouldThrowException_WhenEmailExists() {
		// bước 1:chuẩn bị dữ liệu
		UserRequestDTO dto = new UserRequestDTO();
		dto.setEmail("test@gmail.com");
		dto.setUserName("testuser");
		dto.setPassword("123456");
		// bước 2:mock hành vi dependency
		when(userRepository.existsByEmail("test@gmail.com")).thenReturn(true);
		when(userRepository.existsByUserName("testuser")).thenReturn(false);
		// bước 3:
		UserAlreadyExistException exception = assertThrows(UserAlreadyExistException.class,
				() -> userService.createUser(dto));
		assertTrue(exception.getFieldErrors().containsKey("email"));
		assertEquals("Email already exists", exception.getFieldErrors().get("email"));
		verify(userRepository).existsByEmail("test@gmail.com");
		verify(userRepository).existsByUserName("testuser");
		verify(userRepository, never()).save(any());
	}

	@DisplayName("Create user - Throw exception when username already exists")
	@Test
	void createUser_ShouldReturnThrowException_WhenUserNameExist() {

		UserRequestDTO dto = new UserRequestDTO();
		dto.setUserName("testuser");
		dto.setEmail("testemail@gmail.com");
		dto.setPassword("123456");

		when(userRepository.existsByUserName("testuser")).thenReturn(true);
		when(userRepository.existsByEmail("testemail@gmail.com")).thenReturn(false);

		UserAlreadyExistException existException = assertThrows(UserAlreadyExistException.class,
				() -> userService.createUser(dto));

		assertTrue(existException.getFieldErrors().containsKey("userName"));
		assertEquals("Username already exists", existException.getFieldErrors().get("userName"));

		verify(userRepository).existsByEmail("testemail@gmail.com");
		verify(userRepository).existsByUserName("testuser");
		verify(userRepository, never()).save(any());
	}

	@DisplayName("Create user - Throw exception when username and email already exists")
	@Test
	void createUser_ShouldReturnThrowException_WhenUserNameAndEmailExist() {

		UserRequestDTO dto = new UserRequestDTO();
		dto.setUserName("testuser");
		dto.setEmail("testemail@gmail.com");
		dto.setPassword("123456");

		when(userRepository.existsByUserName("testuser")).thenReturn(true);
		when(userRepository.existsByEmail("testemail@gmail.com")).thenReturn(true);

		UserAlreadyExistException existException = assertThrows(UserAlreadyExistException.class,
				() -> userService.createUser(dto));

		assertTrue(existException.getFieldErrors().containsKey("userName"));
		assertTrue(existException.getFieldErrors().containsKey("email"));
		assertEquals("Username already exists", existException.getFieldErrors().get("userName"));
		assertEquals("Email already exists", existException.getFieldErrors().get("email"));

		verify(userRepository).existsByEmail("testemail@gmail.com");
		verify(userRepository).existsByUserName("testuser");
		verify(userRepository, never()).save(any());
	}

	@DisplayName("Create user - Throw NotFoundException when default role USER not found")
	@Test
	void createUser_ShouldThrowNotFoundException_WhenDefaultRoleNotFound() {

		// GIVEN
		UserRequestDTO dto = new UserRequestDTO();
		dto.setUserName("testuser");
		dto.setEmail("testemail@gmail.com");
		dto.setPassword("123456");

		when(userRepository.existsByEmail("testemail@gmail.com")).thenReturn(false);
		when(userRepository.existsByUserName("testuser")).thenReturn(false);

		User user = new User();
		user.setPassword("123456");
		when(userMapper.toEntity(any(UserRequestDTO.class))).thenReturn(user);
		// ROLE_USER không tồn tại
		when(roleRepository.findByRoleName(SecurityRole.ROLE_USER)).thenReturn(null);

		// WHEN + THEN
		NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.createUser(dto));

		// ASSERT nội dung exception
		assertEquals("role: Default role USER not found", exception.getMessage());

		// VERIFY hành vi
		verify(userRepository).existsByEmail("testemail@gmail.com");
		verify(userRepository).existsByUserName("testuser");
		verify(roleRepository).findByRoleName(SecurityRole.ROLE_USER);

		// KHÔNG được lưu user
		verify(userRepository, never()).save(any());
	}

	@DisplayName("Create user - Throw exception when password encoder fails")
	@Test
	void createUser_ShouldThrowException_WhenPasswordEncoderFails() {

		UserRequestDTO dto = new UserRequestDTO();
		dto.setUserName("testuser");
		dto.setEmail("testuser@gmail.com");
		dto.setPassword("123456");

		when(userRepository.existsByEmail("testuser@gmail.com")).thenReturn(false);
		when(userRepository.existsByUserName("testuser")).thenReturn(false);

		User user = new User();
		user.setPassword("123456");

		when(userMapper.toEntity(any(UserRequestDTO.class))).thenReturn(user);
		when(passwordService.encodePassword("123456")).thenThrow(new RuntimeException("Encode failed"));

		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> userService.createUser(dto));

		assertEquals("Encode failed", runtimeException.getMessage());

		verify(passwordService).encodePassword("123456");
		verify(userRepository, never()).save(any());
	}

	// asignRole
	@DisplayName("Assign role DOCTOR successfully - create doctor if not exists")
	@Test
	void assignRole_ShouldCreateDoctor_WhenRoleIsDoctor() {
		AssignRoleRequestDTO dto = new AssignRoleRequestDTO(1, 2, null);

		User user = new User();
		user.setUserId(1);

		Role role = new Role();
		role.setRoleId(2);
		role.setRoleName("DOCTOR");

		when(userRepository.findById(1)).thenReturn(Optional.of(user));
		when(roleRepository.findById(2)).thenReturn(Optional.of(role));
		when(userRepository.save(any(User.class))).thenReturn(user);
		when(doctorsRepository.findByUser(user)).thenReturn(Optional.empty());
		when(doctorsRepository.save(any(Doctors.class))).thenAnswer(invocation -> invocation.getArgument(0));

		when(userMapper.toDTO(user)).thenReturn(new UserResponseDTO());

		UserResponseDTO result = userService.assignRole(dto);

		assertNotNull(result);

		verify(userRepository).save(user);
		verify(doctorsRepository).findByUser(user);
		verify(doctorsRepository).save(any(Doctors.class));
	}

	@DisplayName("Assign role - Throw exception when user not found")
	@Test
	void assignRole_ShouldThrowException_WhenUserNotFound() {
		AssignRoleRequestDTO dto = new AssignRoleRequestDTO(1, 2, null);

		when(userRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> userService.assignRole(dto));

		assertEquals("user: User Not Found", ex.getMessage());

		verify(roleRepository, never()).findById(any());
		verify(userRepository, never()).save(any());
	}

	@DisplayName("Assign role - Throw exception when role not found")
	@Test
	void assignRole_ShouldThrowException_WhenRoleNotFound() {
		AssignRoleRequestDTO dto = new AssignRoleRequestDTO(1, 2, null);

		User user = new User();
		user.setUserId(1);

		when(userRepository.findById(1)).thenReturn(Optional.of(user));
		when(roleRepository.findById(2)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> userService.assignRole(dto));

		assertEquals("role: Role Not Found", ex.getMessage());

		verify(userRepository, never()).save(any());
		verify(doctorsRepository, never()).save(any());
	}

	@DisplayName("Assign role - Should not create doctor when role is not DOCTOR")
	@Test
	void assignRole_ShouldNotCreateDoctor_WhenRoleIsNotDoctor() {
		AssignRoleRequestDTO dto = new AssignRoleRequestDTO(1, 2, null);

		User user = new User();
		user.setUserId(1);

		Role role = new Role();
		role.setRoleId(2);
		role.setRoleName("ADMIN");

		when(userRepository.findById(1)).thenReturn(Optional.of(user));
		when(roleRepository.findById(2)).thenReturn(Optional.of(role));
		when(userRepository.save(any(User.class))).thenReturn(user);
		when(userMapper.toDTO(user)).thenReturn(new UserResponseDTO());

		UserResponseDTO result = userService.assignRole(dto);

		assertNotNull(result);

		verify(doctorsRepository, never()).findByUser(any());
		verify(doctorsRepository, never()).save(any());
	}

	// getAll
	@DisplayName("Get all users paged successfully")
	@Test
	void getAllPaged_ShouldReturnPagedResultSuccessfully() {
		Pageable pageable = TestPageConstants.PAGEABLE_0_2;

		User user1 = new User();
		User user2 = new User();

		Page<User> userPage = new PageImpl<>(List.of(user1, user2), pageable, TestPageConstants.DEFAULT_SIZE);

		when(userRepository.findAll(pageable)).thenReturn(userPage);
		when(userMapper.toDTO(user1)).thenReturn(new UserResponseDTO());
		when(userMapper.toDTO(user2)).thenReturn(new UserResponseDTO());

		PagedResult<UserResponseDTO> result = userService.getAllPaged(pageable);

		assertNotNull(result);
		assertEquals(TestPageConstants.DEFAULT_SIZE, result.getContent().size());
		assertEquals(TestPageConstants.DEFAULT_SIZE, result.getTotalElements());
		assertEquals(1, result.getTotalPages());
		assertEquals(TestPageConstants.DEFAULT_PAGE, result.getCurrentPage());
		assertEquals(TestPageConstants.DEFAULT_SIZE, result.getPageSize());

		verify(userRepository).findAll(pageable);
		verify(userMapper, times(2)).toDTO(any(User.class));
	}

	// findById
	@DisplayName("Find user by id successfully")
	@Test
	void findById_ShouldReturnUser_WhenUserExists() {
		Integer id = 1;

		User user = new User();
		user.setUserId(id);

		UserResponseDTO dto = new UserResponseDTO();

		when(userRepository.findById(id)).thenReturn(Optional.of(user));
		when(userMapper.toDTO(user)).thenReturn(dto);

		UserResponseDTO result = userService.findById(id);

		assertNotNull(result);

		verify(userRepository).findById(id);
		verify(userMapper).toDTO(user);
	}

	@DisplayName("Find user by id - Throw exception when user not found")
	@Test
	void findById_ShouldThrowException_WhenUserNotFound() {
		Integer id = 1;

		when(userRepository.findById(id)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> userService.findById(id));

		assertEquals("user: User Not Found", ex.getMessage());

		verify(userRepository).findById(id);
		verify(userMapper, never()).toDTO(any());
	}

	// updateAdmin
	@DisplayName("Update user successfully")
	@Test
	void update_ShouldUpdateUser_WhenValidRequest() {
		Integer id = 1;

		User user = new User();
		user.setUserId(id);
		user.setEmail("old@gmail.com");

		UserUpdateRequestDTO dto = new UserUpdateRequestDTO();
		dto.setEmail("new@gmail.com");
		dto.setFullName("New Name");

		when(userRepository.findById(id)).thenReturn(Optional.of(user));
		when(userRepository.existsByEmailAndUserIdNot("new@gmail.com", id)).thenReturn(false);
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toDTO(user)).thenReturn(new UserResponseDTO());

		UserResponseDTO result = userService.update(id, dto);

		assertNotNull(result);
		assertEquals("new@gmail.com", user.getEmail());
		assertEquals("New Name", user.getFullName());

		verify(userRepository).save(user);
	}

	@DisplayName("Update user - Throw exception when user not found")
	@Test
	void update_ShouldThrowException_WhenUserNotFound() {
		Integer id = 1;
		UserUpdateRequestDTO dto = new UserUpdateRequestDTO();

		when(userRepository.findById(id)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> userService.update(id, dto));

		assertEquals("user: User Not Found", ex.getMessage());

		verify(userRepository, never()).save(any());
	}

	@DisplayName("Update user - Throw exception when email already exists")
	@Test
	void update_ShouldThrowException_WhenEmailAlreadyExists() {
		Integer id = 1;

		User user = new User();
		user.setUserId(id);
		user.setEmail("old@gmail.com");

		UserUpdateRequestDTO dto = new UserUpdateRequestDTO();
		dto.setEmail("duplicate@gmail.com");

		when(userRepository.findById(id)).thenReturn(Optional.of(user));
		when(userRepository.existsByEmailAndUserIdNot("duplicate@gmail.com", id)).thenReturn(true);

		UserAlreadyExistException ex = assertThrows(UserAlreadyExistException.class, () -> userService.update(id, dto));

		assertTrue(ex.getFieldErrors().containsKey("email"));

		verify(userRepository, never()).save(any());
	}

	// updateStatus
	@DisplayName("Update status successfully when isActive is provided")
	@Test
	void updateStatus_ShouldUpdateUserStatus_WhenIsActiveNotNull() {
		Integer id = 1;

		User user = new User();
		user.setUserId(id);
		user.setActive(false);

		when(userRepository.findById(id)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toDTO(user)).thenReturn(new UserResponseDTO());

		UserResponseDTO result = userService.updateStatus(id, true);

		assertNotNull(result);
		assertTrue(user.getIsActive());

		verify(userRepository).save(user);
	}

	@DisplayName("Update status - Throw exception when user not found")
	@Test
	void updateStatus_ShouldThrowException_WhenUserNotFound() {
		Integer id = 1;

		when(userRepository.findById(id)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> userService.updateStatus(id, true));

		assertEquals("user: User Not Found", ex.getMessage());

		verify(userRepository, never()).save(any());
	}

	// deleteManually
	@DisplayName("Delete user manually successfully")
	@Test
	void deleteManually_ShouldDeleteUser_WhenUserExists() {
		Integer id = 1;

		User user = new User();
		user.setUserId(id);

		when(userRepository.findById(id)).thenReturn(Optional.of(user));

		boolean result = userService.deleteManually(id);

		assertTrue(result);

		verify(userRepository).delete(user);
	}

	@DisplayName("Delete user manually - Throw exception when user not found")
	@Test
	void deleteManually_ShouldThrowException_WhenUserNotFound() {
		Integer id = 1;

		when(userRepository.findById(id)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> userService.deleteManually(id));

		assertEquals("user: User Not Found", ex.getMessage());

		verify(userRepository, never()).delete(any());
	}

	// findByUserName
	@DisplayName("Find user by username successfully")
	@Test
	void findByUserName_ShouldReturnUser_WhenUserExists() {
		String username = "testuser";

		User user = new User();
		user.setUserName(username);

		UserResponseDTO dto = new UserResponseDTO();

		when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
		when(userMapper.toDTO(user)).thenReturn(dto);

		UserResponseDTO result = userService.findByUserName(username);

		assertNotNull(result);

		verify(userRepository).findByUserName(username);
		verify(userMapper).toDTO(user);
	}

	@DisplayName("Find user by username - Throw exception when user not found")
	@Test
	void findByUserName_ShouldThrowException_WhenUserNotFound() {
		String username = "testuser";

		when(userRepository.findByUserName(username)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> userService.findByUserName(username));

		assertEquals("user: User Not Found", ex.getMessage());

		verify(userMapper, never()).toDTO(any());
	}

	// updateProfile
	@DisplayName("Update profile successfully")
	@Test
	void updateProfile_ShouldUpdateProfile_WhenValidRequest() {
		String username = "testuser";

		User user = new User();
		user.setUserName(username);
		user.setEmail("old@gmail.com");

		UserUpdateRequestDTO dto = new UserUpdateRequestDTO();
		dto.setFullName("New Name");
		dto.setEmail("new@gmail.com");

		when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
		when(userRepository.existsByEmailAndUserIdNot("new@gmail.com", user.getUserId())).thenReturn(false);
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toDTO(user)).thenReturn(new UserResponseDTO());

		UserResponseDTO result = userService.updateProfile(username, dto);

		assertNotNull(result);
		assertEquals("new@gmail.com", user.getEmail());

		verify(userRepository).save(user);
	}

	@DisplayName("Update profile - Throw exception when user not found")
	@Test
	void updateProfile_ShouldThrowException_WhenUserNotFound() {
		when(userRepository.findByUserName("testuser")).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class,
				() -> userService.updateProfile("testuser", new UserUpdateRequestDTO()));

		assertEquals("user: User Not Found", ex.getMessage());

		verify(userRepository, never()).save(any());
	}

	@DisplayName("Update profile - Throw exception when email already exists")
	@Test
	void updateProfile_ShouldThrowException_WhenEmailAlreadyExists() {
		String username = "testuser";

		User user = new User();
		user.setUserName(username);
		user.setUserId(1);
		user.setEmail("old@gmail.com");

		UserUpdateRequestDTO dto = new UserUpdateRequestDTO();
		dto.setEmail("duplicate@gmail.com");

		when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
		when(userRepository.existsByEmailAndUserIdNot("duplicate@gmail.com", 1)).thenReturn(true);

		UserAlreadyExistException ex = assertThrows(UserAlreadyExistException.class,
				() -> userService.updateProfile(username, dto));

		assertTrue(ex.getFieldErrors().containsKey("email"));

		verify(userRepository, never()).save(any());
	}

}
