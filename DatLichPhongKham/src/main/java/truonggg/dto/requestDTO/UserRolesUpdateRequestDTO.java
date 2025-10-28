package truonggg.dto.requestDTO;

import jakarta.validation.constraints.NotNull;
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
public class UserRolesUpdateRequestDTO {
	@NotNull(message = "UserRole ID is required")
	private Integer userRoleId;

	private Integer userId;

	private Integer roleId;

	private Boolean isActive;
}
