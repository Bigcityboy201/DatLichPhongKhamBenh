package truonggg.domain.event;

import truonggg.appointment.domain.model.Appointments;
import truonggg.Enum.Appointments_Enum;

/**
 * Domain event published when an appointment is cancelled.
 * This event can be used to trigger side effects like sending cancellation emails,
 * processing refunds, updating notifications, etc.
 */
public class AppointmentCancelledEvent {

	private final Appointments appointment;
	private final Appointments_Enum cancellationReason;
	private final String cancelledBy; // e.g., "USER", "ADMIN", "DOCTOR"

	public AppointmentCancelledEvent(Appointments appointment, Appointments_Enum cancellationReason, String cancelledBy) {
		this.appointment = appointment;
		this.cancellationReason = cancellationReason;
		this.cancelledBy = cancelledBy;
	}

	public Appointments getAppointment() {
		return appointment;
	}

	public Appointments_Enum getCancellationReason() {
		return cancellationReason;
	}

	public String getCancelledBy() {
		return cancelledBy;
	}
}

