package truonggg.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatMessageRequestDTO {

	@NotNull
	private Integer receiverUserId;

	@NotBlank
	private String content;

}


