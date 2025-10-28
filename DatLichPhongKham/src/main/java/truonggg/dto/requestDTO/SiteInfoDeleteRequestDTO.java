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
public class SiteInfoDeleteRequestDTO {

	@NotNull(message = "Site Info ID is required")
	private Integer id;

	private Boolean isActive;
}
