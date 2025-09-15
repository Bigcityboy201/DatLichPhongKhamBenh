package truonggg.Model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

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
	@OneToMany(mappedBy ="user")
	private List<UserRoles>list=new ArrayList();
}
/*•	UserID (PK) – INT, AUTO_INCREMENT
•	FullName – VARCHAR(100)
•	Email – VARCHAR(100), UNIQUE
•	Phone – VARCHAR(20), UNIQUE
•	Username – VARCHAR(50), UNIQUE
•	Password – VARCHAR(255)
•	Address – VARCHAR(255)
•	DateOfBirth – DATE
•	CreatedAt – DATETIME
•	IsActive – BIT
*/