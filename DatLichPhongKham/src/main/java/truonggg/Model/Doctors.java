package truonggg.Model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctors {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private int experienceYears;
	private String description;
	private String imageUrl;
	@Column(columnDefinition = "BIT DEFAULT 0")
	private boolean isActive;
	@Column(columnDefinition = "BIT DEFAULT 0")
	private Boolean isFeatured;
	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName = "userId")
	private User user;
	@OneToMany(mappedBy = "doctors", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<Appointments> list = new ArrayList();
	@OneToMany(mappedBy = "doctors")
	private List<review> list1 = new ArrayList<>();
	@OneToMany(mappedBy = "doctors", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<Schedules> list2 = new ArrayList<>();
	@OneToMany(mappedBy = "doctors")
	private List<RevenueReports> list3 = new ArrayList<>();
	@OneToMany(mappedBy = "doctors")
	private List<DoctorSpecializations> list4 = new ArrayList<>();
	@ManyToOne
	@JoinColumn(name = "departments_id", referencedColumnName = "id")
	private Departments departments;

	public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Boolean getIsFeatured() {
		return isFeatured;
	}

	public void setIsFeatured(Boolean isFeatured) {
		this.isFeatured = isFeatured;
	}
}
