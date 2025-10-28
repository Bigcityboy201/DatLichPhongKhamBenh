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
public class UserRolesRequestDTO {
	@NotNull(message = "User ID is required")
	private Integer userId;

	@NotNull(message = "Role ID is required")
	private Integer roleId;

	private Boolean isActive;
}
