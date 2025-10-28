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
public class SchedulesDeleteRequestDTO {

	@NotNull(message = "Schedule ID is required")
	private Integer id;

	private Boolean isActive;
}
