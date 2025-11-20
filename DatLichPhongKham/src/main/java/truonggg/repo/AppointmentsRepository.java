package truonggg.repo;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import truonggg.Enum.Appointments_Enum;
import truonggg.Model.Appointments;

@Repository
public interface AppointmentsRepository extends JpaRepository<Appointments, Integer> {

	Page<Appointments> findByUser_UserId(Integer userId, Pageable pageable);

	boolean existsByDoctors_IdAndAppointmentDateTimeAndStatusNot(Integer doctorId, LocalDateTime appointmentDateTime,
			Appointments_Enum status);

	boolean existsByDoctors_IdAndAppointmentDateTimeAndStatusNotAndIdNot(Integer doctorId,
			LocalDateTime appointmentDateTime, Appointments_Enum status, Integer appointmentId);
}
