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
import truonggg.dto.reponseDTO.DepartmentsResponseDTO;
import truonggg.dto.requestDTO.DepartmentsDeleteRequestDTO;
import truonggg.dto.requestDTO.DepartmentsRequestDTO;
import truonggg.dto.requestDTO.DepartmentsUpdateRequestDTO;
import truonggg.mapper.DepartmentsMapper;
import truonggg.reponse.SuccessReponse;
import truonggg.service.DepartmentsService;

@RestController
@RequestMapping(path = "/api/departments")
@RequiredArgsConstructor
public class DepartmentsController {

	private final DepartmentsService departmentsService;
	private final DepartmentsMapper departmentsMapper;

	// GET /api/departments - Lấy tất cả
	@GetMapping
	public SuccessReponse<List<DepartmentsResponseDTO>> getAllDepartments() {
		return SuccessReponse.of(this.departmentsService.getAll());
	}

	// POST /api/departments - Tạo mới
	@PostMapping
	@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
	public SuccessReponse<DepartmentsResponseDTO> createDepartment(@RequestBody @Valid final DepartmentsRequestDTO dto) {
		return SuccessReponse.of(this.departmentsService.createDepartment(dto));
	}

	// PUT /api/departments - Cập nhật
	@PutMapping
	@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
	public SuccessReponse<DepartmentsResponseDTO> updateDepartment(@RequestBody @Valid DepartmentsUpdateRequestDTO dto) {
		return SuccessReponse.of(this.departmentsService.update(dto));
	}

	// DELETE /api/departments - Soft delete
	@DeleteMapping
	@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
	public SuccessReponse<Boolean> deleteDepartment(@RequestBody @Valid DepartmentsDeleteRequestDTO dto) {
		return SuccessReponse.of(this.departmentsService.delete(dto));
	}

	// DELETE /api/departments/{id} - Hard delete
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
	public SuccessReponse<Boolean> hardDeleteDepartment(@PathVariable Integer id) {
		return SuccessReponse.of(this.departmentsService.delete(id));
	}
}
