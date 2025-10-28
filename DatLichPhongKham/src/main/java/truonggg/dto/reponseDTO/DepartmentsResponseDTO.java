package truonggg.dto.reponseDTO;

import java.time.LocalDateTime;

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
public class DepartmentsResponseDTO {
	private Integer id;
	private String name;
	private String description;
	private boolean isActive;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
