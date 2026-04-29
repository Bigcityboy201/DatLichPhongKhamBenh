package truonggg.strategy.impl;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import truonggg.Enum.PaymentMethod;
import truonggg.Enum.PaymentStatus;
import truonggg.appointment.domain.model.Appointments;
import truonggg.dto.reponseDTO.QRCodeResponseDTO;
import truonggg.payment.application.QRCodeService;
import truonggg.payment.domain.model.Payments;
import truonggg.user.domain.model.User;
import truonggg.dto.requestDTO.PaymentRequestDTO;
import truonggg.strategy.PaymentStrategy;
import truonggg.constant.BusinessConstants;
import truonggg.utils.DateUtils;

@Component
@RequiredArgsConstructor
public class BankTransferPaymentStrategy implements PaymentStrategy {

	private final QRCodeService qrCodeService;

	@Override
	public PaymentMethod getSupportedMethod() {
		return PaymentMethod.BANK_TRANSFER;
	}

	@Override
	public Payments processPayment(Appointments appointment, PaymentRequestDTO dto, User user) {

		Payments payment = Payments.builder()
				.amount(BusinessConstants.DEFAULT_DEPOSIT_AMOUNT)
				.paymentDate(DateUtils.currentDate())
				.paymentMethod(PaymentMethod.BANK_TRANSFER)
				.status(PaymentStatus.PENDING)
				.appointments(appointment)
				.build();

		payment.setTransactionId("BANK_MB_" + appointment.getId() + "_" + System.currentTimeMillis());
		payment.setPaymentCode("COCLK" + appointment.getId());

		// Tạo URL QR VietQR cho thanh toán chuyển khoản và lưu vào payment
		QRCodeResponseDTO qrInfo = qrCodeService.generateForPayment(payment);
		payment.setPaymentUrl(qrInfo.getQrCodeUrl());

		return payment;
	}
}
