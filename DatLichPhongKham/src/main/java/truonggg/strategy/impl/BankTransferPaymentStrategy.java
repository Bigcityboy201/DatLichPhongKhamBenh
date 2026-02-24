package truonggg.strategy.impl;

import java.util.Date;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import truonggg.Enum.PaymentMethod;
import truonggg.Enum.PaymentStatus;
import truonggg.appointment.domain.model.Appointments;
import truonggg.payment.domain.model.Payments;
import truonggg.user.domain.model.User;
import truonggg.dto.requestDTO.PaymentRequestDTO;
import truonggg.strategy.PaymentStrategy;

@Component
@RequiredArgsConstructor
public class BankTransferPaymentStrategy implements PaymentStrategy {
	private static final double DEFAULT_DEPOSIT_AMOUNT = 2000;

	@Override
	public PaymentMethod getSupportedMethod() {
		return PaymentMethod.BANK_TRANSFER;
	}

	@Override
	public Payments processPayment(Appointments appointment, PaymentRequestDTO dto, User user) {

		Payments payment = Payments.builder().amount(DEFAULT_DEPOSIT_AMOUNT).paymentDate(new Date())
				.paymentMethod(PaymentMethod.BANK_TRANSFER).status(PaymentStatus.PENDING).appointments(appointment)
				.build();

		payment.setTransactionId("BANK_MB_" + appointment.getId() + "_" + System.currentTimeMillis());
		payment.setPaymentCode("COCLK" + appointment.getId());

		// TODO: Re-integrate QRCodeService in DDD-lite structure if needed.

		return payment;
	}
}
