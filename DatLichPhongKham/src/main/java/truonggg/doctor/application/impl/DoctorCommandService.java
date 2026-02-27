package truonggg.doctor.application.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import truonggg.Enum.DayOfWeek;
import truonggg.doctor.domain.model.Doctors;
import truonggg.doctor.infrastructure.DoctorsRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DoctorCommandService {

    private final DoctorsRepository doctorsRepository;

    @Transactional
    public void addSchedule(
            Integer doctorId,
            DayOfWeek dayOfWeek,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        Doctors doctor = doctorsRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        doctor.addSchedule(dayOfWeek, startAt, endAt);

        doctorsRepository.save(doctor);
    }

    @Transactional
    public void removeSchedule(
            Integer doctorId,
            Integer scheduleId
    ) {
        Doctors doctor = doctorsRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        doctor.removeSchedule(scheduleId);

        doctorsRepository.save(doctor);
    }
}