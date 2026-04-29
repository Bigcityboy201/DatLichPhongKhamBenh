package truonggg.strategy.impl;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import truonggg.Enum.Appointments_Enum;
import truonggg.Enum.PaymentMethod;
import truonggg.Enum.PaymentStatus;
import truonggg.appointment.domain.model.Appointments;
import truonggg.payment.domain.model.Payments;
import truonggg.user.domain.model.User;
import truonggg.dto.requestDTO.PaymentRequestDTO;
import truonggg.strategy.PaymentStrategy;
import truonggg.utils.DateUtils;

@Component
@RequiredArgsConstructor
public class CashPaymentStrategy implements PaymentStrategy {

	@Override
	public PaymentMethod getSupportedMethod() {
		return PaymentMethod.CASH;
	}

	@Override
	public Payments processPayment(Appointments appointment, PaymentRequestDTO dto, User user) {

		Payments payment = Payments.builder()
				.amount(100000)
				.paymentDate(DateUtils.currentDate())
				.paymentMethod(PaymentMethod.CASH)
				.status(PaymentStatus.CONFIRMED)
				.appointments(appointment)
				.build();

		payment.setTransactionId("CASH_" + appointment.getId() + "_" + System.currentTimeMillis());

		// Update appointment status
		if (appointment.getStatus() == Appointments_Enum.PENDING
				|| appointment.getStatus() == Appointments_Enum.AWAITING_DEPOSIT) {
			appointment.setStatus(Appointments_Enum.CONFIRMED);
		}

		return payment;
	}
}
