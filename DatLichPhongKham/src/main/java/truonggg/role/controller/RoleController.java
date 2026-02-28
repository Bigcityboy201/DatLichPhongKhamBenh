package truonggg.role.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
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
import truonggg.dto.reponseDTO.RoleResponseDTO;
import truonggg.dto.requestDTO.RoleDeleteRequestDTO;
import truonggg.dto.requestDTO.RoleRequestDTO;
import truonggg.dto.requestDTO.RoleUpdateRequestDTO;
import truonggg.reponse.PagedResult;
import truonggg.reponse.SuccessReponse;
import truonggg.role.application.RoleCommandService;
import truonggg.role.application.RoleQueryService;

@RestController
@RequestMapping(path = "/api/roles")
@RequiredArgsConstructor
public class RoleController {

	private final RoleQueryService roleQueryService;

	private final RoleCommandService roleCommandService;

	// GET /api/roles - Lấy tất cả
	@GetMapping
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<?> getAllRoles(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<RoleResponseDTO> pagedResult = this.roleQueryService.getAll(pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// GET /api/roles/{id} - Lấy theo ID
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<RoleResponseDTO> getRoleById(@PathVariable Integer id) {
		return SuccessReponse.of(this.roleQueryService.findById(id));
	}

	// POST /api/roles - Tạo mới
	@PostMapping
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<RoleResponseDTO> createRole(@RequestBody @Valid final RoleRequestDTO dto) {
		return SuccessReponse.of(this.roleCommandService.createRole(dto));
	}

	// PUT /api/roles - Cập nhật
	@PutMapping
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<RoleResponseDTO> updateRole(@RequestBody @Valid RoleUpdateRequestDTO dto) {
		return SuccessReponse.of(this.roleCommandService.update(dto));
	}

	// DELETE /api/roles - Soft delete
	@DeleteMapping
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<Boolean> deleteRole(@RequestBody @Valid RoleDeleteRequestDTO dto) {
		return SuccessReponse.of(this.roleCommandService.delete(dto));
	}

}
