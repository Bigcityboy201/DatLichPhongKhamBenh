package truonggg.service;

import org.springframework.data.domain.Pageable;

import truonggg.dto.reponseDTO.NotificationsResponseDTO;
import truonggg.reponse.PagedResult;

public interface NotificationsSelfService {

	PagedResult<NotificationsResponseDTO> getByCurrentUser(String username, Pageable pageable);

	PagedResult<NotificationsResponseDTO> getUnreadByCurrentUser(String username, Pageable pageable);

	boolean markAsReadByCurrentUser(Integer id, String username);

	boolean softDeleteByCurrentUser(Integer id, String username);
}


