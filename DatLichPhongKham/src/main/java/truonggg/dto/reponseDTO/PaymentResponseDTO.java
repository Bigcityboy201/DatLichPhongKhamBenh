package truonggg.dto.reponseDTO;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import truonggg.Enum.Appointments_Enum;
import truonggg.Enum.PaymentMethod;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {
	
	private Integer id;
	private Double amount;
	private Date paymentDate;
	private PaymentMethod paymentMethod;
	private Boolean isDeposit;
	private Appointments_Enum status;
	
	// Payment gateway fields
	private String transactionId;
	private String gatewayTransactionNo;
	private String responseCode;
	
	// Appointment info
	private Integer appointmentId;
	
	// Payment URL (chỉ có khi tạo mới thanh toán online)
	private String paymentUrl;
}

