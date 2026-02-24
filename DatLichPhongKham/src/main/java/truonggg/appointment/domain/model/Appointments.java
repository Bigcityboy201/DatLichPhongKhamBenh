package truonggg.appointment.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Check;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import truonggg.Enum.Appointments_Enum;
import truonggg.doctor.domain.Doctors;
import truonggg.payment.domain.model.Payments;
import truonggg.user.domain.model.User;

@Entity
@Check(constraints = "status BETWEEN 0 AND 7")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointments {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private LocalDateTime appointmentDateTime;
	@Enumerated(EnumType.ORDINAL)
	private Appointments_Enum status;
	private String note;
	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "userId")
	private User user;
	@ManyToOne
	@JoinColumn(name = "doctor_id", referencedColumnName = "id")
	private Doctors doctors;
	@OneToMany(mappedBy = "appointments", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Payments> list = new ArrayList();
}


