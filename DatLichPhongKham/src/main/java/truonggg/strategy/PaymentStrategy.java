package truonggg.strategy;

import truonggg.Enum.PaymentMethod;
import truonggg.Model.Appointments;
import truonggg.Model.Payments;
import truonggg.Model.User;
import truonggg.dto.requestDTO.PaymentRequestDTO;

public interface PaymentStrategy {

	PaymentMethod getSupportedMethod();

	Payments processPayment(Appointments appointment, PaymentRequestDTO dto, User user);
}
