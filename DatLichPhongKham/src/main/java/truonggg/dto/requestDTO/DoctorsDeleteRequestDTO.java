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
public class DoctorsDeleteRequestDTO {

	@NotNull(message = "isActive must not be null")
	private Boolean isActive;
}
