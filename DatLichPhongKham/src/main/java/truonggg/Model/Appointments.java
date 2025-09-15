package truonggg.Model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import truonggg.Enum.Appointments_Enum;

@Entity
public class Appointments {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private Date appointmentDate;
	private Appointments_Enum status;
	private String note;
	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "UserID")
	private User user;
	@ManyToOne
	@JoinColumn(name = "doctor_id", referencedColumnName = "id")
	private Doctors doctors;
	@OneToMany(mappedBy = "appointments")
	private List<Payments> list = new ArrayList();
}
