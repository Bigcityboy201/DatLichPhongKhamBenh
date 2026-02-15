package truonggg.Model;

import java.util.Date;

import org.hibernate.annotations.Check;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import truonggg.Enum.PaymentMethod;
import truonggg.Enum.PaymentStatus;

@Entity
@Check(constraints = "payment_method BETWEEN 0 AND 1 AND status BETWEEN 0 AND 3")
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
	private PaymentStatus status;

	// Payment gateway fields (dùng cho Bank Transfer)
	@Column(unique = true, length = 50)
	private String transactionId; // Mã giao dịch unique
	@Column(length = 100)
	private String gatewayTransactionNo; // Mã giao dịch từ gateway (tid từ bank)
	@Column(length = 100)
	private String responseCode; // Thông tin người gửi cho Bank Transfer
	@Column(length = 500)
	private String secureHash; // Hash để verify (nếu cần)
	@Column(length = 1000)
	private String paymentUrl; // URL QR code thanh toán (cho Bank Transfer)

	@Column(length = 50, unique = true)
	private String paymentCode;

	@ManyToOne
	@JoinColumn(name = "appointment_id", referencedColumnName = "id")
	private Appointments appointments;
}
