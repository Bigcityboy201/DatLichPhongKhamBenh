package truonggg.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
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
import truonggg.dto.reponseDTO.RoleResponseDTO;
import truonggg.dto.requestDTO.RoleDeleteRequestDTO;
import truonggg.dto.requestDTO.RoleRequestDTO;
import truonggg.dto.requestDTO.RoleUpdateRequestDTO;
import truonggg.reponse.SuccessReponse;
import truonggg.service.RoleService;

@RestController
@RequestMapping(path = "/api/roles")
@RequiredArgsConstructor
public class RoleController {

	private final RoleService roleService;

	// GET /api/roles - Lấy tất cả
	@GetMapping
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<List<RoleResponseDTO>> getAllRoles() {
		return SuccessReponse.of(this.roleService.getAll());
	}

	// GET /api/roles/{id} - Lấy theo ID
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<RoleResponseDTO> getRoleById(@PathVariable Integer id) {
		return SuccessReponse.of(this.roleService.findById(id));
	}

	// POST /api/roles - Tạo mới
	@PostMapping
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<RoleResponseDTO> createRole(@RequestBody @Valid final RoleRequestDTO dto) {
		return SuccessReponse.of(this.roleService.createRole(dto));
	}

	// PUT /api/roles - Cập nhật
	@PutMapping
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<RoleResponseDTO> updateRole(@RequestBody @Valid RoleUpdateRequestDTO dto) {
		return SuccessReponse.of(this.roleService.update(dto));
	}

	// DELETE /api/roles - Soft delete
	@DeleteMapping
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<Boolean> deleteRole(@RequestBody @Valid RoleDeleteRequestDTO dto) {
		return SuccessReponse.of(this.roleService.delete(dto));
	}

	// DELETE /api/roles/{id} - Hard delete
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<Boolean> hardDeleteRole(@PathVariable Integer id) {
		return SuccessReponse.of(this.roleService.delete(id));
	}
}
