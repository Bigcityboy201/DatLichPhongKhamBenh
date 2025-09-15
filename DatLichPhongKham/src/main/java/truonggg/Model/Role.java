package truonggg.Model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Role {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer RoleID;
	private String RoleName;
	private String Description;
	private boolean IsActive;
	@OneToMany(mappedBy ="role")
	private List<UserRoles>list=new ArrayList();
}
/*
 * •	RoleID (PK) – INT, AUTO_INCREMENT
•	RoleName – VARCHAR(50) (VD: ADMIN, DOCTOR, PATIENT)
•	Description – VARCHAR(255)
•	IsActive – BIT
*/
 