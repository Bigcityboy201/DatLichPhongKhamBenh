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
public class NotificationsUpdateRequestDTO {

	@NotNull(message = "Notification ID is required")
	private Integer id;

	private Integer userId;

	@Size(max = 1000, message = "Message cannot exceed 1000 characters")
	private String message;

	private Boolean isRead;
}
