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
import truonggg.dto.reponseDTO.SchedulesReponseDTO;
import truonggg.dto.requestDTO.SchedulesRequestDTO;
import truonggg.dto.requestDTO.SchedulesUpdateRequestDTO;
import truonggg.reponse.PagedResult;
import truonggg.reponse.SuccessReponse;
import truonggg.service.SchedulesService;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/schedules")
public class SchedulesController {

	private final SchedulesService schedulesService;

	// GET /api/schedules - Lấy tất cả (phân trang)
	@GetMapping
	public SuccessReponse<?> getAllSchedules(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<SchedulesReponseDTO> pagedResult = schedulesService.getAllPaged(pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// GET /api/schedules/doctor/{doctorId} - Lấy theo Doctor ID
	@GetMapping("/doctor/{doctorId}")
	public SuccessReponse<List<SchedulesReponseDTO>> getSchedulesByDoctorId(@PathVariable Integer doctorId) {
		return SuccessReponse.of(this.schedulesService.getByDoctorId(doctorId));
	}

	// POST /api/schedules - Tạo mới
	@PostMapping
	@PreAuthorize("hasAnyAuthority('DOCTOR', 'ADMIN')")
	public SuccessReponse<SchedulesReponseDTO> createSchedule(@RequestBody @Valid SchedulesRequestDTO dto) {
		return SuccessReponse.of(this.schedulesService.save(dto));
	}

	// PUT /api/schedules - Cập nhật
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('DOCTOR', 'ADMIN')")
	public SuccessReponse<SchedulesReponseDTO> updateSchedule(@RequestBody @Valid SchedulesUpdateRequestDTO dto,
			@PathVariable Integer id) {
		return SuccessReponse.of(this.schedulesService.update(id, dto));
	}

	// DELETE /api/schedules - Soft delete
	@PutMapping("/status/{id}")
	@PreAuthorize("hasAnyAuthority('DOCTOR', 'ADMIN')")
	public SuccessReponse<SchedulesReponseDTO> deleteSchedule(@RequestBody @Valid SchedulesUpdateRequestDTO dto,
			@PathVariable Integer id) {
		return SuccessReponse.of(this.schedulesService.delete(id, dto));
	}

	// DELETE /api/schedules/{id} - Hard delete
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public SuccessReponse<String> hardDeleteSchedule(@PathVariable Integer id) {
		this.schedulesService.delete(id);
		return SuccessReponse.of("Xóa thành công lịch làm việc");
	}
}
