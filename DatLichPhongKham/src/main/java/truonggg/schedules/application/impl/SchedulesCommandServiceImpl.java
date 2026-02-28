package truonggg.schedules.application.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import truonggg.Exception.NotFoundException;
import truonggg.schedules.domain.model.Schedules;
import truonggg.doctor.domain.model.Doctors;
import truonggg.doctor.infrastructure.DoctorsRepository;
import truonggg.schedules.infrastructure.SchedulesRepository;
import truonggg.dto.reponseDTO.SchedulesReponseDTO;
import truonggg.dto.requestDTO.SchedulesRequestDTO;
import truonggg.dto.requestDTO.SchedulesUpdateRequestDTO;
import truonggg.schedules.mapper.SchedulesMapper;
import truonggg.schedules.application.SchedulesCommandService;

@Service
@RequiredArgsConstructor
public class SchedulesCommandServiceImpl implements SchedulesCommandService {
    private final SchedulesRepository schedulesRepository;
    private final DoctorsRepository doctorsRepository;
    private final SchedulesMapper schedulesMapper;

    @Override
    @Transactional
    public SchedulesReponseDTO save(SchedulesRequestDTO dto) {

        Doctors doctor = this.doctorsRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found!"));

        // Lịch (Schedules) là aggregate root độc lập, chỉ tham chiếu Doctor
        Schedules schedule = Schedules.create(
                dto.getDayOfWeek(),
                dto.getStartAt(),
                dto.getEndAt(),
                doctor
        );

        schedule = schedulesRepository.save(schedule);

        return schedulesMapper.toDTO(schedule);
    }

    @Override
    @Transactional
    public SchedulesReponseDTO update(Integer id, SchedulesUpdateRequestDTO dto) {
        Schedules foundSchedule = this.schedulesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("schedule", "Schedule Not Found"));

        // Nếu DTO có doctorId mới, chỉ đơn giản validate sự tồn tại (không thay đổi chủ sở hữu ở đây)
        if (dto.getDoctorId() != null) {
            doctorsRepository.findById(dto.getDoctorId())
                    .orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));
        }

        if (dto.getDayOfWeek() != null) {
            foundSchedule.changeDay(dto.getDayOfWeek());
        }
        if (dto.getStartAt() != null || dto.getEndAt() != null) {
            // Lấy thời gian mới, nếu null thì giữ nguyên giá trị cũ
            var newStart = dto.getStartAt() != null ? dto.getStartAt() : foundSchedule.getStartAt();
            var newEnd = dto.getEndAt() != null ? dto.getEndAt() : foundSchedule.getEndAt();
            foundSchedule.changeTime(newStart, newEnd);
        }

        // JPA dirty checking sẽ lưu thay đổi trong transaction
        return schedulesMapper.toDTO(foundSchedule);
    }

    @Override
    public SchedulesReponseDTO delete(Integer id, SchedulesUpdateRequestDTO dto) {
        Schedules foundSchedule = this.schedulesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("schedule", "Schedule Not Found"));

        if (dto.getActive() == null) {
            throw new IllegalArgumentException("Active status is required");
        }

        if (dto.getActive()) {
            foundSchedule.activate();
        } else {
            foundSchedule.deactivate();
        }
        return this.schedulesMapper.toDTO(foundSchedule);
    }

}