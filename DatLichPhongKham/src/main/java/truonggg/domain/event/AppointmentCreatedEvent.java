package truonggg.domain.event;

import truonggg.appointment.domain.model.Appointments;

/**
 * Domain event published when a new appointment is created.
 * This event can be used to trigger side effects like sending confirmation emails,
 * creating notifications, etc.
 */
public class AppointmentCreatedEvent {

	private final Appointments appointment;

	public AppointmentCreatedEvent(Appointments appointment) {
		this.appointment = appointment;
	}

	public Appointments getAppointment() {
		return appointment;
	}
}

