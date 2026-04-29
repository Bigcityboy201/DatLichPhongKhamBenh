package truonggg.domain.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import truonggg.domain.event.AppointmentCreatedEvent;
import truonggg.domain.event.AppointmentCancelledEvent;

/**
 * Event listener for appointment-related domain events.
 * Handles side effects like sending emails, creating notifications, processing refunds, etc.
 */
@Component
public class AppointmentEventListener {

	private static final Logger logger = LoggerFactory.getLogger(AppointmentEventListener.class);

	/**
	 * Handles AppointmentCreatedEvent asynchronously.
	 * Can be extended to send confirmation emails, create notifications, etc.
	 */
	@Async
	@EventListener
	public void handleAppointmentCreated(AppointmentCreatedEvent event) {
		logger.info("Appointment created event received: appointmentId={}, userId={}, doctorId={}, dateTime={}", 
			event.getAppointment().getId(),
			event.getAppointment().getUser() != null ? event.getAppointment().getUser().getUserId() : null,
			event.getAppointment().getDoctors() != null ? event.getAppointment().getDoctors().getId() : null,
			event.getAppointment().getAppointmentDateTime());
		
		// TODO: Send confirmation email to user
		// TODO: Send notification to doctor (if assigned)
		// TODO: Create notification for user
	}

	/**
	 * Handles AppointmentCancelledEvent asynchronously.
	 * Can be extended to send cancellation emails, process refunds, update notifications, etc.
	 */
	@Async
	@EventListener
	public void handleAppointmentCancelled(AppointmentCancelledEvent event) {
		logger.info("Appointment cancelled event received: appointmentId={}, reason={}, cancelledBy={}", 
			event.getAppointment().getId(),
			event.getCancellationReason(),
			event.getCancelledBy());
		
		// TODO: Send cancellation email to user
		// TODO: Process refund if applicable
		// TODO: Send notification to doctor
		// TODO: Update notifications
	}
}

