package truonggg.domain.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import truonggg.domain.event.PaymentCompletedEvent;

/**
 * Event listener for payment-related domain events.
 * Handles side effects like sending confirmation emails, updating appointment status, etc.
 */
@Component
public class PaymentEventListener {

	private static final Logger logger = LoggerFactory.getLogger(PaymentEventListener.class);

	/**
	 * Handles PaymentCompletedEvent asynchronously.
	 * Can be extended to send confirmation emails, update appointment status, create notifications, etc.
	 */
	@Async
	@EventListener
	public void handlePaymentCompleted(PaymentCompletedEvent event) {
		logger.info("Payment completed event received: paymentId={}, appointmentId={}, amount={}, method={}", 
			event.getPayment().getId(),
			event.getPayment().getAppointments() != null ? event.getPayment().getAppointments().getId() : null,
			event.getPayment().getAmount(),
			event.getPayment().getPaymentMethod());
		
		// TODO: Send payment confirmation email
		// TODO: Update appointment status to CONFIRMED
		// TODO: Create notification for user
		// TODO: Send notification to doctor
	}
}

