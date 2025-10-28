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
public class DepartmentsUpdateRequestDTO {

	@NotNull(message = "Department ID is required")
	private Integer id;

	@Size(max = 255, message = "Name cannot exceed 255 characters")
	private String name;

	@Size(max = 1000, message = "Description cannot exceed 1000 characters")
	private String description;
}
