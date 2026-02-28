package truonggg.appointment.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Check;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import truonggg.Enum.Appointments_Enum;
import truonggg.doctor.domain.model.Doctors;
import truonggg.payment.domain.model.Payments;
import truonggg.user.domain.model.User;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointments {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private LocalDateTime appointmentDateTime;
    @Enumerated(EnumType.STRING)
	private Appointments_Enum status;
	private String note;
	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "userId")
	private User user;
	@ManyToOne
	@JoinColumn(name = "doctor_id", referencedColumnName = "id")
	private Doctors doctors;
	@OneToMany(mappedBy = "appointments")
	private List<Payments> list = new ArrayList();

	// ===== Domain behaviour =====

	public static Appointments create(User user, Doctors doctor, LocalDateTime appointmentDateTime, String note) {
		if (user == null) {
			throw new IllegalArgumentException("User is required");
		}
		if (appointmentDateTime == null) {
			throw new IllegalArgumentException("Thời gian không được để trống");
		}
		if (appointmentDateTime.isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("Thời gian đặt lịch phải ở hiện tại hoặc tương lai");
		}
		Appointments appointment = new Appointments();
		appointment.user = user;
		appointment.doctors = doctor;
		appointment.appointmentDateTime = appointmentDateTime;
		appointment.note = note;
		appointment.status = Appointments_Enum.PENDING;
		return appointment;
	}
    public void reschedule(LocalDateTime newTime) {
        ensureModifiable();

        if (newTime.isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("Cannot reschedule to past");

        this.appointmentDateTime = newTime;
    }

    public void assignDoctor(Doctors doctor) {
        ensureModifiable();

        if (doctor == null)
            throw new IllegalArgumentException("Doctor required");

        this.doctors = doctor;

        if (status == Appointments_Enum.PENDING) {
            this.status = Appointments_Enum.CONFIRMED;
        }
    }

    public void cancel() {
        ensureCancellable();
        this.status = Appointments_Enum.CANCELLED;
    }

    public void cancelWithRefund() {
        ensureCancellable();
        this.status = Appointments_Enum.CANCELLED_REFUND;
    }

    public void cancelWithoutRefund() {
        ensureCancellable();
        this.status = Appointments_Enum.CANCELLED_NO_REFUND;
    }

    private void ensureModifiable() {
        if (status == Appointments_Enum.COMPLETED ||
                status == Appointments_Enum.CANCELLED ||
                status == Appointments_Enum.CANCELLED_NO_REFUND ||
                status == Appointments_Enum.CANCELLED_REFUND) {
            throw new IllegalStateException("Appointment cannot be modified");
        }
    }

    private void ensureCancellable() {
        if (status == Appointments_Enum.COMPLETED ||
                status == Appointments_Enum.CANCELLED) {
            throw new IllegalStateException("Appointment cannot be cancelled");
        }
    }

    public void addPayment(Payments payment) {
        list.add(payment);
        payment.setAppointments(this);
    }

    public void validateBeforeBooking() {

        if (appointmentDateTime == null) {
            throw new IllegalArgumentException("Time required");
        }

        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot book in past");
        }

        validateSlotFormat();
    }

    private void validateSlotFormat() {

        if (appointmentDateTime.getMinute() != 0
                && appointmentDateTime.getMinute() != 30) {
            throw new IllegalArgumentException(
                    "Only 30-minute slots allowed");
        }

        if (appointmentDateTime.getSecond() != 0
                || appointmentDateTime.getNano() != 0) {
            throw new IllegalArgumentException("Invalid time");
        }
    }

    private void validateLunchTime() {

        LocalDateTime lunchStart =
                appointmentDateTime.toLocalDate().atTime(12, 0);

        LocalDateTime lunchEnd =
                appointmentDateTime.toLocalDate().atTime(13, 0);

        if (!appointmentDateTime.isBefore(lunchStart)
                && appointmentDateTime.isBefore(lunchEnd)) {

            throw new IllegalArgumentException(
                    "Cannot book during lunch time");
        }
    }
    public void ensureCanBePaid() {
        if (this.status == Appointments_Enum.CANCELLED) {
            throw new IllegalStateException("Cannot pay cancelled appointment");
        }
    }
}

