package truonggg.Model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer UserID;
	private String FullName;
	private String Email;
	private String Phone;
	private String UserName;
	private String Password;
	private String Address;
	private Date DateOfBirth;
	private Date CreatedAt;
	private boolean IsActive;
	@OneToMany(mappedBy = "user")
	private List<UserRoles> list = new ArrayList();
	@OneToOne(mappedBy = "user")
	private Doctors doctors;
	@OneToMany(mappedBy = "user")
	private List<Appointments> list1 = new ArrayList();
	@OneToMany(mappedBy = "user")
	private List<review> list2 = new ArrayList<>();
	@OneToMany(mappedBy = "user")
	private List<Notifications> list3 = new ArrayList<>();
}
