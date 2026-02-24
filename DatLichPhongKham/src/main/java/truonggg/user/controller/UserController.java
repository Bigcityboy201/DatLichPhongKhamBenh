package truonggg.user.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import truonggg.dto.reponseDTO.UserResponseDTO;
import truonggg.dto.requestDTO.AssignRoleRequestDTO;
import truonggg.dto.requestDTO.UserRequestDTO;
import truonggg.dto.requestDTO.UserStatusDTO;
import truonggg.dto.requestDTO.UserUpdateRequestDTO;
import truonggg.reponse.PagedResult;
import truonggg.reponse.SuccessReponse;
import truonggg.user.application.UserManagementService;
import truonggg.user.application.UserSelfService;

@RestController
@RequestMapping(path = "/api/users")
@RequiredArgsConstructor
public class UserController {
	private final UserManagementService userManagementService;
	private final UserSelfService userSelfService;

	// GET /api/users - Lấy tất cả (phân trang)
	// 1234
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	@GetMapping
	public SuccessReponse<?> getAllUsers(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<UserResponseDTO> pagedResult = userManagementService.getAllPaged(pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// GET /api/users/{id} - Lấy theo ID
	@PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
	@GetMapping("/{id}")
	public SuccessReponse<UserResponseDTO> getUserById(@PathVariable Integer id) {
		return SuccessReponse.of(this.userManagementService.findById(id));
	}

	// POST /api/users - Tạo mới
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	@PostMapping
	public SuccessReponse<UserResponseDTO> createUser(@RequestBody @Valid final UserRequestDTO dto) {
		return SuccessReponse.of(this.userManagementService.createUser(dto));
	}

	@PatchMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<UserResponseDTO> updateUserPartially(@PathVariable Integer id,
			@RequestBody @Valid UserUpdateRequestDTO dto) {
		return SuccessReponse.of(userManagementService.update(id, dto));
	}

	@PatchMapping("/{id}/status")
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<UserResponseDTO> updateUserStatus(@PathVariable(name = "id") Integer id,
			@RequestBody @Valid UserStatusDTO dto) {
		return SuccessReponse.of(userManagementService.updateStatus(id, dto.getActive()));
	}

	// POST /api/users/assign-role - Admin phân role cho user
	@PostMapping("/assign-role")
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<UserResponseDTO> assignRoleToUser(@RequestBody @Valid AssignRoleRequestDTO dto) {
		return SuccessReponse.of(this.userManagementService.assignRole(dto));
	}

	// DELETE /api/users/{id} - Hard delete
	@DeleteMapping("/manually/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<String> hardDeleteUser(@PathVariable Integer id) {
		this.userManagementService.deleteManually(id);
		return SuccessReponse.of("Đã xóa thành công user with id:" + id);
	}

	@GetMapping("/me")
	@PreAuthorize("hasAnyAuthority('USER','EMPLOYEE', 'ADMIN','DOCTOR')")
	public SuccessReponse<UserResponseDTO> getMyProfile() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return SuccessReponse.of(this.userManagementService.findByUserName(username));
	}

	@PutMapping("/profile")
	@PreAuthorize("hasAnyAuthority('USER', 'EMPLOYEE', 'ADMIN')")
	public SuccessReponse<UserResponseDTO> updateMyProfile(@RequestBody @Valid UserUpdateRequestDTO dto) {
		// Lấy username từ token đã được JwtAuthenticationFilter set vào SecurityContext
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return SuccessReponse.of(this.userSelfService.updateProfile(username, dto));
	}
}


