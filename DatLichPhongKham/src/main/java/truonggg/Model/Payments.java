package truonggg.Model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.Check;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import truonggg.Enum.Appointments_Enum;
import truonggg.Enum.PaymentMethod;

@Entity
@Check(constraints = "payment_method BETWEEN 0 AND 2 AND status BETWEEN 0 AND 7")
// Check constraint: payment_method (0-2), status (0-7)
// PaymentMethod: MOMO(0), CASH(1), BANK_TRANSFER(2)
// Appointments_Enum: PENDING(0), CONFIRMED(1), CANCELLED(2), COMPLETED(3), 
//                    AWAITING_DEPOSIT(4), DEPOSIT_PAID(5), CANCELLED_REFUND(6), CANCELLED_NO_REFUND(7)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payments {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private double amount;
	private Date paymentDate;
	@Enumerated(EnumType.ORDINAL)
	private PaymentMethod paymentMethod;
	@Column(columnDefinition = "BIT DEFAULT 0")
	private boolean isDeposit;// 1=dat coc
	@Enumerated(EnumType.ORDINAL)
	private Appointments_Enum status;
	
	// Payment gateway fields (dùng cho MoMo)
	@Column(unique = true, length = 50)
	private String transactionId; // Mã giao dịch unique (orderId cho MoMo)
	@Column(length = 100)
	private String gatewayTransactionNo; // Mã giao dịch từ gateway (transId cho MoMo)
	@Column(length = 20)
	private String responseCode; // Mã phản hồi (resultCode cho MoMo)
	@Column(length = 500)
	private String secureHash; // Hash để verify (signature cho MoMo)
	@Column(length = 1000)
	private String paymentUrl; // URL thanh toán (cho MoMo)
	
	@ManyToOne
	@JoinColumn(name = "appointment_id", referencedColumnName = "id")
	private Appointments appointments;
}
