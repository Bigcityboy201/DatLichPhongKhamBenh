package truonggg.Model;

import org.hibernate.annotations.ManyToAny;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class UserRoles {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer UserRoleID;
	@ManyToOne
	@JoinColumn(name = "user_id",referencedColumnName ="UserID")
	private User user;
	@ManyToOne
	@JoinColumn(name = "role_id",referencedColumnName ="RoleID")
	private Role role;
	private boolean IsActive;
}
/*
•	UserRoleID (PK) – INT, AUTO_INCREMENT
•	UserID (FK → Users)
•	RoleID (FK → Roles)
•	IsActive – BIT
*/