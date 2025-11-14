package truonggg.controller;

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
import truonggg.dto.reponseDTO.AppointmentsResponseDTO;
import truonggg.dto.requestDTO.AppointmentsRequestDTO;
import truonggg.dto.requestDTO.AppointmentsUpdateRequestDTO;
import truonggg.reponse.PagedResult;
import truonggg.reponse.SuccessReponse;
import truonggg.service.AppointmentsService;

@RestController
@RequestMapping(path = "/api/appointments")
@RequiredArgsConstructor
public class AppointmentsController {
	private final AppointmentsService appointmentsService;

	// GET /api/appointments - Lấy tất cả (phân trang)
	@GetMapping
	@PreAuthorize("hasAnyAuthority('DOCTOR', 'ADMIN')")
	public SuccessReponse<?> getAllAppointments(
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<AppointmentsResponseDTO> pagedResult = appointmentsService.getAllPaged(pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// GET /api/appointments/{id} - Lấy theo ID
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('USER', 'DOCTOR', 'EMPLOYEE', 'ADMIN')")
	public SuccessReponse<AppointmentsResponseDTO> getAppointmentById(@PathVariable Integer id) {
		return SuccessReponse.of(this.appointmentsService.findById(id));
	}

	// POST /api/appointments - Tạo mới
	@PostMapping
	public SuccessReponse<AppointmentsResponseDTO> createAppointment(
			@RequestBody @Valid final AppointmentsRequestDTO dto) {
		return SuccessReponse.of(this.appointmentsService.createAppointments(dto));
	}

	// PUT /api/appointments - Cập nhật
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('DOCTOR', 'ADMIN')")
	public SuccessReponse<AppointmentsResponseDTO> updateAppointment(@PathVariable Integer id,
			@RequestBody @Valid AppointmentsUpdateRequestDTO dto) {
		return SuccessReponse.of(this.appointmentsService.update(id, dto));
	}

	// DELETE /api/appointments - Soft delete
	@PutMapping("/{id}/status")
	@PreAuthorize("hasAnyAuthority('DOCTOR', 'ADMIN')")
    public SuccessReponse<AppointmentsResponseDTO> deleteAppointment(@PathVariable Integer id) {
		return SuccessReponse.of(this.appointmentsService.delete(id));
	}

	// DELETE /api/appointments/{id} - Hard delete
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('DOCTOR', 'ADMIN')")
	public SuccessReponse<String> hardDeleteAppointment(@PathVariable Integer id) {
		this.appointmentsService.deleteManually(id);
		return SuccessReponse.of("Xóa thành công!");
	}
}
