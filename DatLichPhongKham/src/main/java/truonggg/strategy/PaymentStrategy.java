package truonggg.strategy;

import truonggg.Enum.PaymentMethod;
import truonggg.appointment.domain.model.Appointments;
import truonggg.payment.domain.model.Payments;
import truonggg.user.domain.model.User;
import truonggg.dto.requestDTO.PaymentRequestDTO;

public interface PaymentStrategy {

	PaymentMethod getSupportedMethod();

	Payments processPayment(Appointments appointment, PaymentRequestDTO dto, User user);
}
