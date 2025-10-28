package truonggg.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import truonggg.dto.reponseDTO.UserRolesResponseDTO;
import truonggg.dto.requestDTO.UserRolesDeleteRequestDTO;
import truonggg.dto.requestDTO.UserRolesRequestDTO;
import truonggg.dto.requestDTO.UserRolesUpdateRequestDTO;
import truonggg.reponse.SuccessReponse;
import truonggg.service.UserRolesService;

@RestController
@RequestMapping(path = "/api/user-roles")
@RequiredArgsConstructor
public class UserRolesController {

	private final UserRolesService userRolesService;

	// GET /api/user-roles - Lấy tất cả
	@GetMapping
	public SuccessReponse<List<UserRolesResponseDTO>> getAllUserRoles() {
		return SuccessReponse.of(this.userRolesService.getAll());
	}

	// GET /api/user-roles/{id} - Lấy theo ID
	@GetMapping("/{id}")
	public SuccessReponse<UserRolesResponseDTO> getUserRoleById(@PathVariable Integer id) {
		return SuccessReponse.of(this.userRolesService.findById(id));
	}

	// GET /api/user-roles/user/{userId} - Lấy theo User ID
	@GetMapping("/user/{userId}")
	public SuccessReponse<List<UserRolesResponseDTO>> getUserRolesByUserId(@PathVariable Integer userId) {
		return SuccessReponse.of(this.userRolesService.getByUserId(userId));
	}

	// GET /api/user-roles/user/{userId}/active - Lấy active theo User ID
	@GetMapping("/user/{userId}/active")
	public SuccessReponse<List<UserRolesResponseDTO>> getActiveUserRolesByUserId(@PathVariable Integer userId) {
		return SuccessReponse.of(this.userRolesService.getActiveByUserId(userId));
	}

	// GET /api/user-roles/role/{roleId} - Lấy theo Role ID
	@GetMapping("/role/{roleId}")
	public SuccessReponse<List<UserRolesResponseDTO>> getUserRolesByRoleId(@PathVariable Integer roleId) {
		return SuccessReponse.of(this.userRolesService.getByRoleId(roleId));
	}

	// GET /api/user-roles/role/{roleId}/active - Lấy active theo Role ID
	@GetMapping("/role/{roleId}/active")
	public SuccessReponse<List<UserRolesResponseDTO>> getActiveUserRolesByRoleId(@PathVariable Integer roleId) {
		return SuccessReponse.of(this.userRolesService.getActiveByRoleId(roleId));
	}

	// GET /api/user-roles/check - Kiểm tra có role
	@GetMapping("/check")
	public SuccessReponse<Boolean> checkUserRole(@RequestParam Integer userId, @RequestParam Integer roleId) {
		return SuccessReponse.of(this.userRolesService.hasRole(userId, roleId));
	}

	// GET /api/user-roles/check-active - Kiểm tra có role active
	@GetMapping("/check-active")
	public SuccessReponse<Boolean> checkActiveUserRole(@RequestParam Integer userId, @RequestParam Integer roleId) {
		return SuccessReponse.of(this.userRolesService.hasActiveRole(userId, roleId));
	}

	// POST /api/user-roles - Gán role
	@PostMapping
	public SuccessReponse<UserRolesResponseDTO> assignUserRole(@RequestBody @Valid final UserRolesRequestDTO dto) {
		return SuccessReponse.of(this.userRolesService.assignRole(dto));
	}

	// PUT /api/user-roles - Cập nhật
	@PutMapping
	public SuccessReponse<UserRolesResponseDTO> updateUserRole(@RequestBody @Valid UserRolesUpdateRequestDTO dto) {
		return SuccessReponse.of(this.userRolesService.update(dto));
	}

	// DELETE /api/user-roles - Soft delete
	@DeleteMapping
	public SuccessReponse<Boolean> deleteUserRole(@RequestBody @Valid UserRolesDeleteRequestDTO dto) {
		return SuccessReponse.of(this.userRolesService.delete(dto));
	}

	// DELETE /api/user-roles/{id} - Hard delete
	@DeleteMapping("/{id}")
	public SuccessReponse<Boolean> hardDeleteUserRole(@PathVariable Integer id) {
		return SuccessReponse.of(this.userRolesService.delete(id));
	}
}
