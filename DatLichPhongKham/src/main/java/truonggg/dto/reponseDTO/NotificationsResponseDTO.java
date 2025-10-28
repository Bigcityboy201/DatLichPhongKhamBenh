package truonggg.dto.reponseDTO;

import java.util.Date;

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
public class NotificationsResponseDTO {
	private Integer id;
	private String message;
	private Boolean isRead;
	private Date createdAt;
	private Integer userId;
	private String userName;
}
