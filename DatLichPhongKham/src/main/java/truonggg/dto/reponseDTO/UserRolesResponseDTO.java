package truonggg.dto.reponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRolesResponseDTO {
	private Integer userRoleId;
	private Integer userId;
	private String userName;
	private String userEmail;
	private Integer roleId;
	private String roleName;
	private Boolean isActive;
}
