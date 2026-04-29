package truonggg.dto.reponseDTO;

import java.time.LocalDateTime;

import lombok.Data;
import truonggg.chat.domain.model.ChatMessage.SenderType;

@Data
public class ChatMessageResponseDTO {

	private Integer id;

	private Integer senderUserId;

	private Integer receiverUserId;

	private String senderName;

	private String receiverName;

	private SenderType senderType;

	private String content;

	private LocalDateTime sentAt;

	private boolean read;
}


