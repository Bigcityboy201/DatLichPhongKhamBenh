package truonggg.chat.application;

import truonggg.dto.reponseDTO.ChatMessageResponseDTO;
import truonggg.dto.requestDTO.ChatMessageRequestDTO;

/**
 * Application service: chứa business logic cho use-case gửi tin nhắn,
 * entity/domain chỉ đóng vai trò model dữ liệu.
 */
public interface ChatCommandService {

    ChatMessageResponseDTO sendMessage(ChatMessageRequestDTO request, String username);
}


