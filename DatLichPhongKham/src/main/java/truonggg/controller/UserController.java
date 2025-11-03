package truonggg.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import truonggg.dto.reponseDTO.UserResponseDTO;
import truonggg.dto.requestDTO.AssignRoleRequestDTO;
import truonggg.dto.requestDTO.UserRequestDTO;
import truonggg.dto.requestDTO.UserStatusDTO;
import truonggg.dto.requestDTO.UserUpdateRequestDTO;
import truonggg.reponse.SuccessReponse;
import truonggg.service.UserService;

@RestController
@RequestMapping(path = "/api/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	// GET /api/users - Lấy tất cả
	@GetMapping
	public SuccessReponse<List<UserResponseDTO>> getAllUsers() {
		return SuccessReponse.of(this.userService.getAll());
	}

	// GET /api/users/{id} - Lấy theo ID
	@GetMapping("/{id}")
	public SuccessReponse<UserResponseDTO> getUserById(@PathVariable Integer id) {
		return SuccessReponse.of(this.userService.findById(id));
	}

	// POST /api/users - Tạo mới
	@PostMapping
	public SuccessReponse<UserResponseDTO> createUser(@RequestBody @Valid final UserRequestDTO dto) {
		return SuccessReponse.of(this.userService.createUser(dto));
	}

	@PatchMapping("/{id}")
	public SuccessReponse<UserResponseDTO> updateUserPartially(@PathVariable Integer id,
			@RequestBody @Valid UserUpdateRequestDTO dto) {
		return SuccessReponse.of(userService.update(id, dto));
	}

	@PatchMapping("/{id}/status")
	public SuccessReponse<UserResponseDTO> updateUserStatus(@PathVariable(name = "id") Integer id,
			@RequestBody @Valid UserStatusDTO dto) {
		return SuccessReponse.of(userService.updateStatus(id, dto.getActive()));
	}

	// POST /api/users/assign-role - Admin phân role cho user
	@PostMapping("/assign-role")
	public SuccessReponse<Boolean> assignRoleToUser(@RequestBody @Valid AssignRoleRequestDTO dto) {
		return SuccessReponse.of(this.userService.assignRole(dto));
	}

	// DELETE /api/users/{id} - Hard delete
	@DeleteMapping("/manually/{id}")
	public SuccessReponse<String> hardDeleteUser(@PathVariable Integer id) {
		this.userService.deleteManually(id);
		return SuccessReponse.of("Đã xóa thành công user with id:" + id);
	}
}
