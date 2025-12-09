package truonggg.controller;

import java.util.List;

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
import truonggg.dto.reponseDTO.AppointmentsResponseDTO;
import truonggg.dto.reponseDTO.DoctorsReponseDTO;
import truonggg.dto.reponseDTO.SchedulesReponseDTO;
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
	public SuccessReponse<List<DoctorsReponseDTO>> getDoctorsByDepartment(@RequestParam(required = false) Integer id,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		Pageable pageable = PageRequest.of(page, size);
		PagedResult<DoctorsReponseDTO> pagedResult = doctorsService.getDoctorsByDepartmentPaged(id, pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// POST /api/doctors - Tạo mới
	@PostMapping
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<DoctorsReponseDTO> createDoctor(@RequestBody @Valid final DoctorsRequestDTO dto) {
		return SuccessReponse.of(this.doctorsService.createDoctor(dto));
	}

	// PUT /api/doctors/profile - Cập nhật profile (cho DOCTOR)
	@PutMapping("/{id}/profile")
	@PreAuthorize("hasAnyAuthority('DOCTOR', 'ADMIN')")
	public SuccessReponse<DoctorsReponseDTO> updateDoctorProfile(@PathVariable Integer id,
			@RequestBody @Valid DoctorUpdateRequestDTO dto) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return SuccessReponse.of(this.doctorsService.updateProfile(id, dto, username));
	}

	// PUT /api/doctors - Cập nhật (cho ADMIN/EMPLOYEE)
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<DoctorsReponseDTO> updateDoctor(@PathVariable Integer id,
			@RequestBody @Valid DoctorUpdateRequestDTO dto) {
		return SuccessReponse.of(this.doctorsService.updateWithUser(id, dto));
	}

	// DELETE /api/doctors - Soft delete
	@PatchMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<DoctorsReponseDTO> deleteDoctor(@PathVariable Integer id,
			@RequestBody @Valid DoctorsDeleteRequestDTO dto) {
		return SuccessReponse.of(this.doctorsService.delete(id, dto));
	}

	// DELETE /api/doctors/{id} - Hard delete
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<String> hardDeleteDoctor(@PathVariable Integer id) {
		this.doctorsService.deleteManually(id);
		return SuccessReponse.of("Xóa thành công doctor với id:" + id);
	}

	@GetMapping("/search")
	public SuccessReponse<List<DoctorsReponseDTO>> searchDoctors(@RequestParam String keyword,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		Pageable pageable = PageRequest.of(page, size);
		PagedResult<DoctorsReponseDTO> pagedResult = doctorsService.searchDoctors(keyword, pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// GET /api/doctors/me - Xem profile bác sĩ đang đăng nhập
	@GetMapping("/me")
	@PreAuthorize("hasAnyAuthority('DOCTOR')")
	public SuccessReponse<DoctorsReponseDTO> getMyProfile() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return SuccessReponse.of(this.doctorsService.findByUserName(username));
	}

	// GET /api/doctors/me/appointments - Xem cuộc hẹn của bác sĩ đang đăng nhập
	@GetMapping("/me/appointments")
	@PreAuthorize("hasAnyAuthority('DOCTOR')")
	public SuccessReponse<?> getMyAppointments(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<AppointmentsResponseDTO> pagedResult = doctorsService.getMyAppointments(username, pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// GET /api/doctors/me/schedules - Xem lịch làm việc của bác sĩ đang đăng nhập
	@GetMapping("/me/schedules")
	@PreAuthorize("hasAnyAuthority('DOCTOR')")
	public SuccessReponse<?> getMySchedules(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<SchedulesReponseDTO> pagedResult = this.doctorsService.getMySchedules(username, pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}
}
