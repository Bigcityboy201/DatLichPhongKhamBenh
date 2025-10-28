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
import truonggg.dto.reponseDTO.AppointmentsResponseDTO;
import truonggg.dto.requestDTO.AppointmentsDeleteRequestDTO;
import truonggg.dto.requestDTO.AppointmentsRequestDTO;
import truonggg.dto.requestDTO.AppointmentsUpdateRequestDTO;
import truonggg.reponse.SuccessReponse;
import truonggg.service.AppointmentsService;

@RestController
@RequestMapping(path = "/api/appointments")
@RequiredArgsConstructor
public class AppointmentsController {
	private final AppointmentsService appointmentsService;

	// GET /api/appointments - Lấy tất cả
	@GetMapping
	public SuccessReponse<List<AppointmentsResponseDTO>> getAllAppointments() {
		return SuccessReponse.of(this.appointmentsService.getAll());
	}

	// GET /api/appointments/{id} - Lấy theo ID
	@GetMapping("/{id}")
    public SuccessReponse<AppointmentsResponseDTO> getAppointmentById(@PathVariable Integer id) {
		return SuccessReponse.of(this.appointmentsService.findById(id));
	}

	// POST /api/appointments - Tạo mới
	@PostMapping
	public SuccessReponse<AppointmentsResponseDTO> createAppointment(@RequestBody @Valid final AppointmentsRequestDTO dto) {
		return SuccessReponse.of(this.appointmentsService.createAppointments(dto));
	}

	// PUT /api/appointments - Cập nhật
	@PutMapping
	public SuccessReponse<AppointmentsResponseDTO> updateAppointment(@RequestBody @Valid AppointmentsUpdateRequestDTO dto) {
		return SuccessReponse.of(this.appointmentsService.update(dto));
	}

	// DELETE /api/appointments - Soft delete
	@DeleteMapping
	public SuccessReponse<Boolean> deleteAppointment(@RequestBody @Valid AppointmentsDeleteRequestDTO dto) {
		return SuccessReponse.of(this.appointmentsService.delete(dto));
	}

	// DELETE /api/appointments/{id} - Hard delete
	@DeleteMapping("/{id}")
    public SuccessReponse<Boolean> hardDeleteAppointment(@PathVariable Integer id) {
		return SuccessReponse.of(this.appointmentsService.delete(id));
	}
}
