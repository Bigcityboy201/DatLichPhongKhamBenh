package truonggg.chat.application;

import java.util.List;

import truonggg.dto.reponseDTO.ChatMessageResponseDTO;

public interface ChatQueryService {

	/**
	 * Lấy lịch sử hội thoại giữa user hiện tại (từ username) và partnerUserId.
	 */
	List<ChatMessageResponseDTO> getConversation(Integer partnerUserId, String username);
}


