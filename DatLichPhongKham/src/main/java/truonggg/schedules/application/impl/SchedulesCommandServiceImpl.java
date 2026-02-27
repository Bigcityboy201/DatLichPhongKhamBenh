package truonggg.schedules.application.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import truonggg.reponse.PagedResult;
import truonggg.schedules.application.SchedulesCommandService;
import truonggg.schedules.application.SchedulesQueryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchedulesServiceImpl implements SchedulesCommandService {
    private final SchedulesRepository schedulesRepository;

    private final DoctorsRepository doctorsRepository;

    private final SchedulesMapper schedulesMapper;

    @Override
    public SchedulesReponseDTO save(SchedulesRequestDTO dto) {
        Doctors doctors = this.doctorsRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found!"));
        Schedules schedules =Schedules.create(dto.getDayOfWeek(),dto.getStartAt(),dto.getEndAt(),doctors);
        return this.schedulesMapper.toDTO(schedulesRepository.save(schedules));
    }

    @Override
    public SchedulesReponseDTO update(Integer id, SchedulesUpdateRequestDTO dto) {
        Schedules foundSchedule = this.schedulesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("schedule", "Schedule Not Found"));

        foundSchedule.changeDay(dto.getDayOfWeek());
        foundSchedule.changeTime(dto.getStartAt(),dto.getEndAt());

        // Cập nhật doctor nếu có
        if (dto.getDoctorId() != null) {
            Doctors doctors = this.doctorsRepository.findById(dto.getDoctorId())
                    .orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));
            foundSchedule.assignDoctor(doctors);
        }

        return this.schedulesMapper.toDTO(this.schedulesRepository.save(foundSchedule));
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

    @Transactional
    @Override
    public boolean delete(Integer id) {
        Schedules foundSchedule = this.schedulesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("schedule", "Schedule Not Found"));

        this.schedulesRepository.delete(foundSchedule);
        return true;
    }
}