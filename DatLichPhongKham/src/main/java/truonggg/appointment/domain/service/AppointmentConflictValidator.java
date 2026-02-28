package truonggg.appointment.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import truonggg.Enum.Appointments_Enum;
import truonggg.appointment.infrastructure.AppointmentsRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AppointmentConflictValidator {

    private final AppointmentsRepository repository;

//    public AppointmentConflictValidator(
//            AppointmentsRepository repository) {
//        this.repository = repository;
//    }

    public void validate(Integer doctorId,
                         LocalDateTime time,
                         Integer excludeId) {

        boolean busy;

        if (excludeId == null) {
            busy = repository
                    .existsByDoctors_IdAndAppointmentDateTimeAndStatusNot(
                            doctorId, time,
                            Appointments_Enum.CANCELLED);
        } else {
            busy = repository
                    .existsByDoctors_IdAndAppointmentDateTimeAndStatusNotAndIdNot(
                            doctorId, time,
                            Appointments_Enum.CANCELLED,
                            excludeId);
        }

        if (busy) {
            throw new IllegalArgumentException("Slot already booked");
        }
    }
}