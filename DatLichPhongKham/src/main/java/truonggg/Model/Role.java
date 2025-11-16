package truonggg.Model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
public class Role {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "RoleID")
	private Integer roleId;
	@Column(name = "RoleName")
	private String roleName;
	private String Description;
	@Column(columnDefinition = "BIT DEFAULT 0")
	private boolean isActive;
//	@OneToMany(mappedBy = "role")
//	private List<UserRoles> list = new ArrayList();
	@OneToMany(mappedBy = "role")
	private List<User> users = new ArrayList();

	// Thêm method thủ công cho boolean isActive
	public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}
}
