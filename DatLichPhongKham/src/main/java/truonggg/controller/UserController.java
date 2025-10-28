package truonggg.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import truonggg.dto.reponseDTO.UserResponseDTO;
import truonggg.dto.requestDTO.AssignRoleRequestDTO;
import truonggg.dto.requestDTO.UserDeleteRequestDTO;
import truonggg.dto.requestDTO.UserRequestDTO;
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

	// PUT /api/users - Cập nhật
	@PutMapping
	public SuccessReponse<UserResponseDTO> updateUser(@RequestBody @Valid UserUpdateRequestDTO dto) {
		return SuccessReponse.of(this.userService.update(dto));
	}

	// DELETE /api/users - Soft delete
	@DeleteMapping
	public SuccessReponse<Boolean> deleteUser(@RequestBody @Valid UserDeleteRequestDTO dto) {
		return SuccessReponse.of(this.userService.delete(dto));
	}

	// POST /api/users/assign-role - Admin phân role cho user
	@PostMapping("/assign-role")
	public SuccessReponse<Boolean> assignRoleToUser(@RequestBody @Valid AssignRoleRequestDTO dto) {
		return SuccessReponse.of(this.userService.assignRole(dto));
	}

	// DELETE /api/users/{id} - Hard delete
	@DeleteMapping("/{id}")
	public SuccessReponse<Boolean> hardDeleteUser(@PathVariable Integer id) {
		return SuccessReponse.of(this.userService.delete(id));
	}
}
