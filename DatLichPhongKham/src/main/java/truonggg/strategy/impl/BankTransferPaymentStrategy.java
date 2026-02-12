package truonggg.strategy.impl;

import java.util.Date;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import truonggg.Enum.PaymentMethod;
import truonggg.Enum.PaymentStatus;
import truonggg.Model.Appointments;
import truonggg.Model.Payments;
import truonggg.Model.User;
import truonggg.dto.requestDTO.PaymentRequestDTO;
import truonggg.service.QRCodeService;
import truonggg.strategy.PaymentStrategy;

@Component
@RequiredArgsConstructor
public class BankTransferPaymentStrategy implements PaymentStrategy {

	private final QRCodeService qrCodeService;
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

		var qr = qrCodeService.generateQRCode(DEFAULT_DEPOSIT_AMOUNT, appointment.getId());
		payment.setPaymentUrl(qr.getQrCodeUrl());

		return payment;
	}
}
