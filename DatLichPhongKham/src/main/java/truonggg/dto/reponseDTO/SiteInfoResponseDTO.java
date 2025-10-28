package truonggg.dto.reponseDTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteInfoResponseDTO {
	private Integer id;
	private String infoKey;
	private String value;
	private boolean isActive;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
