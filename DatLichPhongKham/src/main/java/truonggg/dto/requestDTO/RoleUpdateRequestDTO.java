package truonggg.dto.requestDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class RoleUpdateRequestDTO {

	@NotNull(message = "Role ID is required")
	private Integer roleId;

	@Size(max = 50, message = "Role name cannot exceed 50 characters")
	private String roleName;

	@Size(max = 255, message = "Description cannot exceed 255 characters")
	private String description;
}
