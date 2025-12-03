package truonggg.dto.requestDTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDTO {
	
	@NotNull(message = "Appointment ID không được bỏ trống")
	private Integer appointmentId;
	
	@NotNull(message = "Số tiền không được bỏ trống")
	@Min(value = 1000, message = "Số tiền tối thiểu là 1,000 VNĐ")
	private Double amount;
	
	@NotNull(message = "Phương thức thanh toán không được bỏ trống")
	private String paymentMethod; // MOMO, CASH, BANK_TRANSFER
}

