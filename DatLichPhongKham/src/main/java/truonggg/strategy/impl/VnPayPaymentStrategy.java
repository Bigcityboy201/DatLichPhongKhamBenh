package truonggg.strategy.impl;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import truonggg.Enum.PaymentMethod;
import truonggg.Enum.PaymentStatus;
import truonggg.appointment.domain.model.Appointments;
import truonggg.payment.application.VnPayService;
import truonggg.payment.domain.model.Payments;
import truonggg.user.domain.model.User;
import truonggg.dto.requestDTO.PaymentRequestDTO;
import truonggg.strategy.PaymentStrategy;
import truonggg.constant.BusinessConstants;
import truonggg.utils.DateUtils;

/**
 * Chiến lược thanh toán qua VNPAY.
 * FE chỉ cần gửi paymentMethod = "VNPAY".
 * Backend sẽ trả về PaymentResponseDTO với paymentUrl để FE redirect.
 */
@Component
@RequiredArgsConstructor
public class VnPayPaymentStrategy implements PaymentStrategy {

    private final VnPayService vnPayService;

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.VNPAY;
    }

    @Override
    public Payments processPayment(Appointments appointment, PaymentRequestDTO dto, User user) {

        Payments payment = Payments.builder()
                .amount(BusinessConstants.DEFAULT_DEPOSIT_AMOUNT)
                .paymentDate(DateUtils.currentDate())
                .paymentMethod(PaymentMethod.VNPAY)
                .status(PaymentStatus.PENDING)
                .appointments(appointment)
                .build();

        // Mã giao dịch & mã đơn hàng dùng cho VNPAY
        payment.setTransactionId("VNPAY_" + appointment.getId() + "_" + System.currentTimeMillis());
        payment.setPaymentCode("VNPAYLK" + appointment.getId());

        // Tạo URL thanh toán VNPAY và lưu vào paymentUrl
        String paymentUrl = vnPayService.createPaymentUrl(payment);
        payment.setPaymentUrl(paymentUrl);

        return payment;
    }
}


