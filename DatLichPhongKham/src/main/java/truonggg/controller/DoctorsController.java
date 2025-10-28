package truonggg.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import truonggg.dto.reponseDTO.DoctorsReponseDTO;
import truonggg.dto.requestDTO.DoctorUpdateRequestDTO;
import truonggg.dto.requestDTO.DoctorsDeleteRequestDTO;
import truonggg.dto.requestDTO.DoctorsRequestDTO;
import truonggg.reponse.PagedResult;
import truonggg.reponse.SuccessReponse;
import truonggg.service.DoctorsService;

@RestController
@RequestMapping(path = "/api/doctors")
@RequiredArgsConstructor
public class DoctorsController {
	private final DoctorsService doctorsService;

	// GET /api/doctors - Lấy tất cả (có thể phân trang)
	@GetMapping
	public SuccessReponse<?> getAllDoctors(@RequestParam(required = false) Boolean featured,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		// Nếu có featured parameter, trả về danh sách không phân trang
		if (featured != null) {
			return SuccessReponse.of(this.doctorsService.getAll(featured));
		}

		// Nếu không có featured parameter, trả về danh sách có phân trang
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<DoctorsReponseDTO> pagedResult = doctorsService.getAllPaged(pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// GET /api/doctors/{id} - Lấy theo ID
	@GetMapping("/{id}")
	public SuccessReponse<DoctorsReponseDTO> getDoctorById(@PathVariable Integer id) {
		return SuccessReponse.of(this.doctorsService.findById(id));
	}

	// GET /api/doctors/department - Lấy theo Department
	@GetMapping("/department")
	public SuccessReponse<List<DoctorsReponseDTO>> getDoctorsByDepartment(@RequestParam Integer id,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		Pageable pageable = PageRequest.of(page, size);
		PagedResult<DoctorsReponseDTO> pagedResult = doctorsService.getDoctorsByDepartmentPaged(id, pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// POST /api/doctors - Tạo mới
	@PostMapping
	public SuccessReponse<DoctorsReponseDTO> createDoctor(@RequestBody @Valid final DoctorsRequestDTO dto) {
		return SuccessReponse.of(this.doctorsService.createDoctor(dto));
	}

	// PUT /api/doctors/profile - Cập nhật profile (cho DOCTOR)
	@PutMapping("/profile")
	public SuccessReponse<DoctorsReponseDTO> updateDoctorProfile(@RequestBody @Valid DoctorUpdateRequestDTO dto) {
		return SuccessReponse.of(this.doctorsService.updateProfile(dto));
	}

	// PUT /api/doctors - Cập nhật (cho ADMIN/EMPLOYEE)
	@PutMapping
	public SuccessReponse<DoctorsReponseDTO> updateDoctor(@RequestBody @Valid DoctorUpdateRequestDTO dto) {
		return SuccessReponse.of(this.doctorsService.updateWithUser(dto));
	}

	// DELETE /api/doctors - Soft delete
	@DeleteMapping
	public SuccessReponse<Boolean> deleteDoctor(@RequestBody @Valid DoctorsDeleteRequestDTO dto) {
		return SuccessReponse.of(this.doctorsService.delete(dto));
	}

	// DELETE /api/doctors/{id} - Hard delete
	@DeleteMapping("/{id}")
	public SuccessReponse<Boolean> hardDeleteDoctor(@PathVariable Integer id) {
		return SuccessReponse.of(this.doctorsService.delete(id));
	}
}
