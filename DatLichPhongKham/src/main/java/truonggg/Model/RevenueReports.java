package truonggg.Model;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class RevenueReports {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private Date reportDate;
	private double totalRevenue;
	private int totalAppointments;
	private int totalCompletedAppointments;
	private int totalCanceledAppointments;
	private Date createdAt;
	private boolean isActive;
	@ManyToOne
	@JoinColumn(name = "doctor_id", referencedColumnName = "id")
	private Doctors doctors;
}
