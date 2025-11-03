package truonggg.Model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer userId;
	private String fullName;
	private String email;
	private String phone;
	private String userName;
	private String password;
	private String address;
	private Date dateOfBirth;
	private Date createdAt;
	@Column(columnDefinition = "BIT DEFAULT 0")
	private boolean isActive;

	// Thay đổi từ OneToMany sang ManyToOne - mỗi user chỉ có 1 role
	@ManyToOne
	@JoinColumn(name = "role_id", referencedColumnName = "roleId")
	private Role role;

	@OneToOne(mappedBy = "user")
	private Doctors doctors;
	@OneToMany(mappedBy = "user")
	private List<Appointments> list1 = new ArrayList();
	@OneToMany(mappedBy = "user")
	private List<review> list2 = new ArrayList<>();
	@OneToMany(mappedBy = "user")
	private List<Notifications> list3 = new ArrayList<>();

//	public boolean isActive() {
//		return isActive;
//	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean getIsActive() {
		return isActive;
	}
}
