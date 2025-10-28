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
public class SiteInfoUpdateRequestDTO {

	@NotNull(message = "Site Info ID is required")
	private Integer id;

	@Size(max = 255, message = "Info key cannot exceed 255 characters")
	private String infoKey;

	@Size(max = 1000, message = "Value cannot exceed 1000 characters")
	private String value;
}
