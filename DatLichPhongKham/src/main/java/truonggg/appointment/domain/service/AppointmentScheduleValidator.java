package truonggg.appointment.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import truonggg.schedules.infrastructure.SchedulesRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AppointmentScheduleValidator {

    private final SchedulesRepository schedulesRepository;

//    public AppointmentScheduleValidator(SchedulesRepository repo) {
//        this.schedulesRepository = repo;
//    }

    public void validate(Integer doctorId, LocalDateTime time) {

        boolean hasSchedule =
                schedulesRepository
                        .existsByDoctors_IdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
                                doctorId, time, time);

        if (!hasSchedule) {
            throw new IllegalArgumentException(
                    "Time not in doctor's schedule");
        }
    }
}