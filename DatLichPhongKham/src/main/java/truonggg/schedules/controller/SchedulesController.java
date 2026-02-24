package truonggg.schedules.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import truonggg.dto.reponseDTO.SchedulesReponseDTO;
import truonggg.dto.requestDTO.SchedulesRequestDTO;
import truonggg.dto.requestDTO.SchedulesUpdateRequestDTO;
import truonggg.reponse.PagedResult;
import truonggg.reponse.SuccessReponse;
import truonggg.schedules.application.SchedulesCommandService;
import truonggg.schedules.application.SchedulesQueryService;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/schedules")
public class SchedulesController {

    private final SchedulesQueryService schedulesQueryService;

    private final SchedulesCommandService schedulesCommandService;

    // GET /api/schedules - Lấy tất cả (phân trang)
    @GetMapping
    @PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
    public SuccessReponse<?> getAllSchedules(@RequestParam(value = "page", defaultValue = "0") int page,
                                             @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResult<SchedulesReponseDTO> pagedResult = this.schedulesQueryService.getAllPaged(pageable);
        return SuccessReponse.ofPaged(pagedResult);
    }

    // GET /api/schedules/doctor/{doctorId} - Lấy theo Doctor ID
    @GetMapping("/doctor/{doctorId}")
    public SuccessReponse<?> getSchedulesByDoctorId(@PathVariable Integer doctorId,
                                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                                    @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResult<SchedulesReponseDTO> pagedResult = this.schedulesQueryService.getByDoctorId(doctorId, pageable);
        return SuccessReponse.ofPaged(pagedResult);
    }

    // POST /api/schedules - Tạo mới
    @PostMapping
    @PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
    public SuccessReponse<SchedulesReponseDTO> createSchedule(@RequestBody @Valid SchedulesRequestDTO dto) {
        return SuccessReponse.of(this.schedulesCommandService.save(dto));
    }

    // PUT /api/schedules - Cập nhật
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
    public SuccessReponse<SchedulesReponseDTO> updateSchedule(@RequestBody @Valid SchedulesUpdateRequestDTO dto,
                                                              @PathVariable Integer id) {
        return SuccessReponse.of(this.schedulesCommandService.update(id, dto));
    }

    // DELETE /api/schedules - Soft delete
    @PutMapping("/status/{id}")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
    public SuccessReponse<SchedulesReponseDTO> deleteSchedule(@RequestBody @Valid SchedulesUpdateRequestDTO dto,
                                                              @PathVariable Integer id) {
        return SuccessReponse.of(this.schedulesCommandService.delete(id, dto));
    }

    // DELETE /api/schedules/{id} - Hard delete
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    public SuccessReponse<String> hardDeleteSchedule(@PathVariable Integer id) {
        this.schedulesCommandService.delete(id);
        return SuccessReponse.of("Xóa thành công lịch làm việc");
    }
}
