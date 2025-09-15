package truonggg.Model;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import truonggg.Enum.Appointments_Enum;
import truonggg.Enum.PaymentMethod;

@Entity
public class Payments {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private double amount;
	private Date paymentDate;
	private PaymentMethod paymentMethod;
	private boolean isDeposit;// 1=dat coc
	private Appointments_Enum status;
	@ManyToOne
	@JoinColumn(name = "appointment_id", referencedColumnName = "id")
	private Appointments appointments;
}
