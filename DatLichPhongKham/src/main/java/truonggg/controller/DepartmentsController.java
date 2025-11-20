package truonggg.controller;

import java.util.List;

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
import truonggg.dto.reponseDTO.DepartmentsResponseDTO;
import truonggg.dto.requestDTO.DepartmentsRequestDTO;
import truonggg.dto.requestDTO.DepartmentsUpdateRequestDTO;
import truonggg.mapper.DepartmentsMapper;
import truonggg.reponse.PagedResult;
import truonggg.reponse.SuccessReponse;
import truonggg.service.DepartmentsService;

@RestController
@RequestMapping(path = "/api/departments")
@RequiredArgsConstructor
public class DepartmentsController {

	private final DepartmentsService departmentsService;
	private final DepartmentsMapper departmentsMapper;

	// GET /api/departments - Lấy tất cả (phân trang)
	@GetMapping
	public SuccessReponse<?> getAllDepartments(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<DepartmentsResponseDTO> pagedResult = departmentsService.getAllPaged(pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// POST /api/departments - Tạo mới
	@PostMapping
	@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
	public SuccessReponse<DepartmentsResponseDTO> createDepartment(
			@RequestBody @Valid final DepartmentsRequestDTO dto) {
		return SuccessReponse.of(this.departmentsService.createDepartment(dto));
	}

	// PUT /api/departments - Cập nhật
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
	public SuccessReponse<DepartmentsResponseDTO> updateDepartment(@RequestBody @Valid DepartmentsUpdateRequestDTO dto,
			@PathVariable Integer id) {
		return SuccessReponse.of(this.departmentsService.update(id, dto));
	}

	// DELETE /api/departments - Soft delete
	@PutMapping("/status/{id}")
	@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
	public SuccessReponse<DepartmentsResponseDTO> deleteDepartment(@RequestBody @Valid DepartmentsUpdateRequestDTO dto,
			@PathVariable Integer id) {
		return SuccessReponse.of(this.departmentsService.delete(id, dto));
	}

	// DELETE /api/departments/{id} - Hard delete
	@DeleteMapping("/manually/{id}")
	@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
	public SuccessReponse<String> hardDeleteDepartment(@PathVariable Integer id) {
		this.departmentsService.delete(id);
		return SuccessReponse.of("Xóa thành công khoa");
	}

	@GetMapping("/search")
	public SuccessReponse<List<DepartmentsResponseDTO>> searchDepartments(@RequestParam String keyword,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		Pageable pageable = PageRequest.of(page, size);
		PagedResult<DepartmentsResponseDTO> pagedResult = departmentsService.searchDepartments(keyword, pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}
}
