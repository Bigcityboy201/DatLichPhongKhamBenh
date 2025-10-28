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
import truonggg.dto.reponseDTO.SchedulesReponseDTO;
import truonggg.dto.requestDTO.SchedulesDeleteRequestDTO;
import truonggg.dto.requestDTO.SchedulesRequestDTO;
import truonggg.dto.requestDTO.SchedulesUpdateRequestDTO;
import truonggg.reponse.SuccessReponse;
import truonggg.service.SchedulesService;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/schedules")
public class SchedulesController {

	private final SchedulesService schedulesService;

	// GET /api/schedules - Lấy tất cả
	@GetMapping
	public SuccessReponse<List<SchedulesReponseDTO>> getAllSchedules() {
		return SuccessReponse.of(this.schedulesService.getAll());
	}

	// GET /api/schedules/doctor/{doctorId} - Lấy theo Doctor ID
	@GetMapping("/doctor/{doctorId}")
	public SuccessReponse<List<SchedulesReponseDTO>> getSchedulesByDoctorId(@PathVariable Integer doctorId) {
		return SuccessReponse.of(this.schedulesService.getByDoctorId(doctorId));
	}

	// POST /api/schedules - Tạo mới
	@PostMapping
	public SuccessReponse<SchedulesReponseDTO> createSchedule(@RequestBody @Valid SchedulesRequestDTO dto) {
		return SuccessReponse.of(this.schedulesService.save(dto));
	}

	// PUT /api/schedules - Cập nhật
	@PutMapping
	public SuccessReponse<SchedulesReponseDTO> updateSchedule(@RequestBody @Valid SchedulesUpdateRequestDTO dto) {
		return SuccessReponse.of(this.schedulesService.update(dto));
	}

	// DELETE /api/schedules - Soft delete
	@DeleteMapping
	public SuccessReponse<Boolean> deleteSchedule(@RequestBody @Valid SchedulesDeleteRequestDTO dto) {
		return SuccessReponse.of(this.schedulesService.delete(dto));
	}

	// DELETE /api/schedules/{id} - Hard delete
	@DeleteMapping("/{id}")
	public SuccessReponse<Boolean> hardDeleteSchedule(@PathVariable Integer id) {
		return SuccessReponse.of(this.schedulesService.delete(id));
	}
}
