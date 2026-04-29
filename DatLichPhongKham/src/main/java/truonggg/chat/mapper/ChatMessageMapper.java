package truonggg.chat.mapper;

import org.mapstruct.Mapper;

import truonggg.chat.domain.model.ChatMessage;
import truonggg.dto.reponseDTO.ChatMessageResponseDTO;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

	// MapStruct sẽ map các field trùng tên (id, senderUserId, receiverUserId, senderType, content, sentAt, read)
	ChatMessageResponseDTO toDto(ChatMessage entity);
}


