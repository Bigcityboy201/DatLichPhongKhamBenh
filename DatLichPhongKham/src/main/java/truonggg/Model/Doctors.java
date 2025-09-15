package truonggg.Model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Doctors {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private int ExperienceYears;
	private String Description;
	private String ImageUrl;
	private boolean IsActive;
	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName = "UserID")
	private User user;
	@OneToMany(mappedBy = "doctors")
	private List<Appointments> list = new ArrayList();
	@OneToMany(mappedBy = "doctors")
	private List<review> list1 = new ArrayList<>();
	@OneToMany(mappedBy = "doctors")
	private List<Schedules> list2 = new ArrayList<>();
	@OneToMany(mappedBy = "doctors")
	private List<RevenueReports> list3 = new ArrayList<>();
	@OneToMany(mappedBy = "doctors")
	private List<DoctorSpecializations> list4 = new ArrayList<>();
}
