package truonggg.dto.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignRoleRequestDTO {
	
	@NotNull(message = "User ID is required")
	private Integer userId;
	
	@NotNull(message = "Role ID is required")
	private Integer roleId;
	
	private String reason;
}
