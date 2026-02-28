package truonggg.appointment.domain.service;

import org.springframework.stereotype.Component;
import truonggg.payment.domain.model.Payments;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class AppointmentCancellationService {

    private static final int REFUND_WINDOW_MINUTES = 10;

    public boolean shouldRefund(Payments payment) {

        if (payment == null || !payment.isDeposit()) {
            return false;
        }

        if (payment.getPaymentDate() == null) {
            return false;
        }

        LocalDateTime paymentTime =
                payment.getPaymentDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime();

        long minutes =
                Duration.between(paymentTime, LocalDateTime.now()).toMinutes();

        return minutes <= REFUND_WINDOW_MINUTES;
    }
}