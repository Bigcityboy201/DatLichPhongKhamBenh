package truonggg.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import truonggg.Model.Notifications;
import truonggg.dto.reponseDTO.NotificationsResponseDTO;
import truonggg.dto.requestDTO.NotificationsRequestDTO;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface NotificationsMapper {
	
	@Mapping(target = "id", source = "id")
	@Mapping(target = "message", source = "message")
	@Mapping(target = "isRead", ignore = true)
	@Mapping(target = "createdAt", source = "createdAt")
	@Mapping(target = "userId", source = "user.userId")
	@Mapping(target = "userName", source = "user.fullName")
	NotificationsResponseDTO toDTO(Notifications notification);

	// Không dùng mapper cho toEntity, sẽ tạo thủ công trong service
}
