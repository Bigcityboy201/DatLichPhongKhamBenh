package truonggg.doctor.controller;

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
import truonggg.dto.reponseDTO.DoctorSummaryResponseDTO;
import truonggg.dto.reponseDTO.DoctorsReponseDTO;
import truonggg.dto.reponseDTO.SchedulesReponseDTO;
import truonggg.dto.requestDTO.DoctorUpdateRequestDTO;
import truonggg.dto.requestDTO.DoctorsDeleteRequestDTO;
import truonggg.dto.requestDTO.DoctorsRequestDTO;
import truonggg.reponse.PagedResult;
import truonggg.reponse.SuccessReponse;
import truonggg.doctor.application.DoctorAdminService;
import truonggg.doctor.application.DoctorQueryService;
import truonggg.doctor.application.DoctorSelfService;

@RestController
@RequestMapping(path = "/api/doctors")
@RequiredArgsConstructor
public class DoctorsController {
	private final DoctorQueryService doctorQueryService;
	private final DoctorAdminService doctorAdminService;
	private final DoctorSelfService doctorSelfService;

	// GET /api/doctors - Lấy tất cả (có thể phân trang)
	@GetMapping
	public SuccessReponse<?> getAllDoctors(@RequestParam(required = false) Boolean featured,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		// Nếu có featured parameter, trả về danh sách không phân trang
		if (featured != null) {
			return SuccessReponse.of(this.doctorQueryService.getAll(featured));
		}

		// Nếu không có featured parameter, trả về danh sách có phân trang
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<DoctorSummaryResponseDTO> pagedResult = doctorQueryService.getAllPaged(pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// GET /api/doctors/{id} - Lấy theo ID
	@GetMapping("/{id}")
	public SuccessReponse<DoctorsReponseDTO> getDoctorById(@PathVariable Integer id) {
		return SuccessReponse.of(this.doctorQueryService.findById(id));
	}

	// GET /api/doctors/department - Lấy theo Department
	@GetMapping("/department")
	public SuccessReponse<List<DoctorSummaryResponseDTO>> getDoctorsByDepartment(
			@RequestParam(required = false) Integer id, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		Pageable pageable = PageRequest.of(page, size);
		PagedResult<DoctorSummaryResponseDTO> pagedResult = doctorQueryService.getDoctorsByDepartmentPaged(id,
				pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// PUT /api/doctors/profile - Cập nhật profile (cho DOCTOR)
	@PutMapping("/profile")
	@PreAuthorize("hasAnyAuthority('DOCTOR', 'ADMIN')")
	public SuccessReponse<DoctorSummaryResponseDTO> updateDoctorProfile(
			@RequestBody @Valid DoctorUpdateRequestDTO dto) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return SuccessReponse.of(this.doctorSelfService.updateProfile(dto, username));
	}

	// PUT /api/doctors - Cập nhật (cho ADMIN/EMPLOYEE)
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<DoctorSummaryResponseDTO> updateDoctor(@PathVariable Integer id,
			@RequestBody @Valid DoctorUpdateRequestDTO dto) {
		return SuccessReponse.of(this.doctorAdminService.updateWithUser(id, dto));
	}

	// DELETE /api/doctors - Soft delete
	@PatchMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<DoctorSummaryResponseDTO> deleteDoctor(@PathVariable Integer id,
			@RequestBody @Valid DoctorsDeleteRequestDTO dto) {
		return SuccessReponse.of(this.doctorAdminService.delete(id, dto));
	}

	// DELETE /api/doctors/{id} - Hard delete
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<String> hardDeleteDoctor(@PathVariable Integer id) {
		this.doctorAdminService.deleteManually(id);
		return SuccessReponse.of("Xóa thành công doctor với id:" + id);
	}

	@GetMapping("/search")
	public SuccessReponse<List<DoctorSummaryResponseDTO>> searchDoctors(@RequestParam String keyword,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		Pageable pageable = PageRequest.of(page, size);
		PagedResult<DoctorSummaryResponseDTO> pagedResult = doctorQueryService.searchDoctors(keyword, pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// GET /api/doctors/me - Xem profile bác sĩ đang đăng nhập
	@GetMapping("/me")
	@PreAuthorize("hasAnyAuthority('DOCTOR')")
	public SuccessReponse<DoctorsReponseDTO> getMyProfile() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return SuccessReponse.of(this.doctorSelfService.findByUserName(username));
	}

	// GET /api/doctors/me/appointments - Xem cuộc hẹn của bác sĩ đang đăng nhập
	@GetMapping("/me/appointments")
	@PreAuthorize("hasAnyAuthority('DOCTOR')")
	public SuccessReponse<?> getMyAppointments(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<AppointmentsResponseDTO> pagedResult = doctorSelfService.getMyAppointments(username, pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// GET /api/doctors/me/schedules - Xem lịch làm việc của bác sĩ đang đăng nhập
	@GetMapping("/me/schedules")
	@PreAuthorize("hasAnyAuthority('DOCTOR')")
	public SuccessReponse<?> getMySchedules(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<SchedulesReponseDTO> pagedResult = this.doctorSelfService.getMySchedules(username, pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}
}


