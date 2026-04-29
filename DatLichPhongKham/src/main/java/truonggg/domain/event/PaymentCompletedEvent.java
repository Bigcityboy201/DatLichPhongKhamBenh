package truonggg.domain.event;

import truonggg.payment.domain.model.Payments;

/**
 * Domain event published when a payment is completed/confirmed.
 * This event can be used to trigger side effects like sending confirmation emails,
 * updating appointment status, creating notifications, etc.
 */
public class PaymentCompletedEvent {

	private final Payments payment;

	public PaymentCompletedEvent(Payments payment) {
		this.payment = payment;
	}

	public Payments getPayment() {
		return payment;
	}
}

