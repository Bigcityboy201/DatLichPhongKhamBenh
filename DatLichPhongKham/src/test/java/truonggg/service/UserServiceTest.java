package truonggg.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import truonggg.Exception.NotFoundException;
import truonggg.Exception.UserAlreadyExistException;
import truonggg.constant.SecurityRole;
import truonggg.doctor.application.impl.DoctorRoleAssignmentHandler;
import truonggg.role.application.RoleAssignmentHandler;
import truonggg.role.domain.model.Role;
import truonggg.role.infrastructure.RoleRepository;
import truonggg.dto.reponseDTO.UserResponseDTO;
import truonggg.dto.requestDTO.AssignRoleRequestDTO;
import truonggg.dto.requestDTO.UserRequestDTO;
import truonggg.dto.requestDTO.UserUpdateRequestDTO;
import truonggg.user.application.PasswordService;
import truonggg.user.application.impl.UserServiceIMPL;
import truonggg.user.domain.model.User;
import truonggg.user.infrastructure.UserRepository;
import truonggg.user.mapper.UserMapper;
import truonggg.reponse.PagedResult;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordService passwordService;

    @Mock
    private RoleRepository roleRepository;

    private UserServiceIMPL userService;

    @BeforeEach
    void setUp() {
        List<RoleAssignmentHandler> handlers = List.of(new DoctorRoleAssignmentHandler());
        userService = new UserServiceIMPL(userRepository, userMapper, passwordService, roleRepository, handlers);
        userService.init(); // build handlerMap như @PostConstruct
    }

    // ===================== CREATE =====================

    @Test
    @DisplayName("createUser: success when email & username do not exist")
    void createUser_shouldReturnUserResponse_WhenUserDoesNotExist() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setFullName("Test User");
        dto.setEmail("test@gmail.com");
        dto.setUserName("testuser");
        dto.setPassword("123456");

        Role roleUser = new Role();
        roleUser.setRoleName(SecurityRole.ROLE_USER);

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByUserName(dto.getUserName())).thenReturn(false);
        when(roleRepository.findByRoleName(SecurityRole.ROLE_USER)).thenReturn(roleUser);
        when(passwordService.encodePassword(dto.getPassword())).thenReturn("encoded-password");

        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setEmail(dto.getEmail());
        responseDTO.setUserName(dto.getUserName());
        when(userMapper.toDTO(any(User.class))).thenReturn(responseDTO);

        UserResponseDTO result = userService.createUser(dto);

        assertNotNull(result);
        assertEquals(dto.getEmail(), result.getEmail());
        assertEquals(dto.getUserName(), result.getUserName());

        verify(userRepository).save(any(User.class));
        verify(userMapper).toDTO(any(User.class));
    }

    @Test
    @DisplayName("createUser: throw when email already exists")
    void createUser_ShouldThrow_WhenEmailExists() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setFullName("Test");
        dto.setEmail("test@gmail.com");
        dto.setUserName("testuser");
        dto.setPassword("123456");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);
        when(userRepository.existsByUserName(dto.getUserName())).thenReturn(false);

        UserAlreadyExistException ex = assertThrows(UserAlreadyExistException.class, () -> userService.createUser(dto));
        assertEquals("Email already exists", ex.getFieldErrors().get("email"));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser: throw when username already exists")
    void createUser_ShouldThrow_WhenUserNameExists() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setFullName("Test");
        dto.setEmail("test@gmail.com");
        dto.setUserName("testuser");
        dto.setPassword("123456");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByUserName(dto.getUserName())).thenReturn(true);

        UserAlreadyExistException ex = assertThrows(UserAlreadyExistException.class, () -> userService.createUser(dto));
        assertEquals("Username already exists", ex.getFieldErrors().get("userName"));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser: throw when default role USER not found")
    void createUser_ShouldThrow_WhenDefaultRoleNotFound() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setFullName("Test");
        dto.setEmail("test@gmail.com");
        dto.setUserName("testuser");
        dto.setPassword("123456");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByUserName(dto.getUserName())).thenReturn(false);
        when(roleRepository.findByRoleName(SecurityRole.ROLE_USER)).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> userService.createUser(dto));
        assertEquals("role: Default role USER not found", ex.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser: propagate exception when password encoder fails")
    void createUser_ShouldThrow_WhenPasswordEncoderFails() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setFullName("Test");
        dto.setEmail("test@gmail.com");
        dto.setUserName("testuser");
        dto.setPassword("123456");

        Role roleUser = new Role();
        roleUser.setRoleName(SecurityRole.ROLE_USER);

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByUserName(dto.getUserName())).thenReturn(false);
        when(roleRepository.findByRoleName(SecurityRole.ROLE_USER)).thenReturn(roleUser);
        when(passwordService.encodePassword(dto.getPassword())).thenThrow(new RuntimeException("Encode failed"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.createUser(dto));
        assertEquals("Encode failed", ex.getMessage());

        verify(userRepository, never()).save(any());
    }

    // ===================== ASSIGN ROLE =====================

    @Test
    @DisplayName("assignRole: ROLE_DOCTOR attaches doctor profile via handler")
    void assignRole_ShouldAttachDoctorProfile_WhenRoleIsDoctor() {
        AssignRoleRequestDTO dto = new AssignRoleRequestDTO(1, 2, null);

        Role roleUser = new Role();
        roleUser.setRoleName(SecurityRole.ROLE_USER);

        Role roleDoctor = new Role();
        roleDoctor.setRoleId(2);
        roleDoctor.setRoleName(SecurityRole.ROLE_DOCTOR);

        User user = User.create("u1", "pw", "Full", "u1@mail.com", roleUser);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(roleRepository.findById(2)).thenReturn(Optional.of(roleDoctor));
        when(userMapper.toDTO(user)).thenReturn(new UserResponseDTO());

        UserResponseDTO result = userService.assignRole(dto);

        assertNotNull(result);
        assertNotNull(user.getDoctors());
    }

    @Test
    @DisplayName("assignRole: throw when user not found")
    void assignRole_ShouldThrow_WhenUserNotFound() {
        AssignRoleRequestDTO dto = new AssignRoleRequestDTO(1, 2, null);

        when(userRepository.findById(1)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> userService.assignRole(dto));
        assertEquals("user: User Not Found", ex.getMessage());
    }

    @Test
    @DisplayName("assignRole: throw when role not found")
    void assignRole_ShouldThrow_WhenRoleNotFound() {
        AssignRoleRequestDTO dto = new AssignRoleRequestDTO(1, 2, null);

        Role roleUser = new Role();
        roleUser.setRoleName(SecurityRole.ROLE_USER);
        User user = User.create("u1", "pw", "Full", "u1@mail.com", roleUser);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(roleRepository.findById(2)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> userService.assignRole(dto));
        assertEquals("role: Role Not Found", ex.getMessage());
    }

    @Test
    @DisplayName("assignRole: non-doctor role does not attach doctor profile")
    void assignRole_ShouldNotAttachDoctorProfile_WhenRoleIsNotDoctor() {
        AssignRoleRequestDTO dto = new AssignRoleRequestDTO(1, 2, null);

        Role roleUser = new Role();
        roleUser.setRoleName(SecurityRole.ROLE_USER);

        Role roleAdmin = new Role();
        roleAdmin.setRoleId(2);
        roleAdmin.setRoleName(SecurityRole.ROLE_ADMIN);

        User user = User.create("u1", "pw", "Full", "u1@mail.com", roleUser);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(roleRepository.findById(2)).thenReturn(Optional.of(roleAdmin));
        when(userMapper.toDTO(user)).thenReturn(new UserResponseDTO());

        UserResponseDTO result = userService.assignRole(dto);

        assertNotNull(result);
        assertNull(user.getDoctors());
    }

    // ===================== QUERY/COMMAND =====================

    @Test
    @DisplayName("getAllPaged: returns paged result")
    void getAllPaged_ShouldReturnPagedResult() {
        Pageable pageable = TestPageConstants.PAGEABLE_0_2;

        User u1 = mock(User.class);
        User u2 = mock(User.class);
        Page<User> page = new PageImpl<>(List.of(u1, u2), pageable, 2);

        when(userRepository.findAll(pageable)).thenReturn(page);
        when(userMapper.toDTO(any(User.class))).thenReturn(new UserResponseDTO());

        PagedResult<UserResponseDTO> result = userService.getAllPaged(pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }

    @Test
    @DisplayName("findById: returns dto when user exists")
    void findById_ShouldReturn_WhenExists() {
        User user = mock(User.class);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(new UserResponseDTO());

        assertNotNull(userService.findById(1));
    }

    @Test
    @DisplayName("findById: throw when user not found")
    void findById_ShouldThrow_WhenNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> userService.findById(1));
        assertEquals("user: User Not Found", ex.getMessage());
    }

    @Test
    @DisplayName("update: calls domain updateProfile when user exists")
    void update_ShouldCallUpdateProfile_WhenExists() {
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1);

        UserUpdateRequestDTO dto = new UserUpdateRequestDTO();
        dto.setFullName("New Name");
        dto.setEmail("new@gmail.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndUserIdNot("new@gmail.com", 1)).thenReturn(false);
        when(userMapper.toDTO(user)).thenReturn(new UserResponseDTO());

        assertNotNull(userService.update(1, dto));
        verify(user).updateProfile(eq("New Name"), eq("new@gmail.com"), any(), any(), any());
    }

    @Test
    @DisplayName("updateStatus: activates when isActive=true")
    void updateStatus_ShouldActivate_WhenTrue() {
        User user = mock(User.class);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(new UserResponseDTO());

        assertNotNull(userService.updateStatus(1, true));
        verify(user).activate();
    }

    @Test
    @DisplayName("deleteManually: deletes when user exists")
    void deleteManually_ShouldDelete_WhenExists() {
        User user = mock(User.class);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        assertTrue(userService.deleteManually(1));
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("findByUserName: returns dto when exists")
    void findByUserName_ShouldReturn_WhenExists() {
        User user = mock(User.class);
        when(userRepository.findByUserName("u")).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(new UserResponseDTO());

        assertNotNull(userService.findByUserName("u"));
    }

    @Test
    @DisplayName("updateProfile: calls domain updateProfile when exists")
    void updateProfile_ShouldCallUpdateProfile_WhenExists() {
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1);

        UserUpdateRequestDTO dto = new UserUpdateRequestDTO();
        dto.setFullName("New Name");
        dto.setEmail("new@gmail.com");

        when(userRepository.findByUserName("u")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndUserIdNot("new@gmail.com", 1)).thenReturn(false);
        when(userMapper.toDTO(user)).thenReturn(new UserResponseDTO());

        assertNotNull(userService.updateProfile("u", dto));
        verify(user).updateProfile(eq("New Name"), eq("new@gmail.com"), any(), any(), any());
    }
}


