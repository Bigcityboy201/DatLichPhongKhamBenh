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

        var schedule = doctor.addSchedule(
                dto.getDayOfWeek(),
                dto.getStartAt(),
                dto.getEndAt()
        );

        doctorsRepository.save(doctor);

        return schedulesMapper.toDTO(schedule);
    }

    @Override
    public SchedulesReponseDTO update(Integer id, SchedulesUpdateRequestDTO dto) {
        Schedules foundSchedule = this.schedulesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("schedule", "Schedule Not Found"));

        Doctors doctor = doctorsRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));

        Schedules schedule = doctor.updateSchedule(
                id,
                dto.getDayOfWeek(),
                dto.getStartAt(),
                dto.getEndAt()
        );

        doctorsRepository.save(doctor);

        return schedulesMapper.toDTO(schedule);
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