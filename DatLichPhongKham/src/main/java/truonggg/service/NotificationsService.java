package truonggg.service;

import org.springframework.data.domain.Pageable;

import truonggg.reponse.PagedResult;
import truonggg.dto.reponseDTO.NotificationsResponseDTO;
import truonggg.dto.requestDTO.NotificationsRequestDTO;
import truonggg.dto.requestDTO.NotificationsUpdateRequestDTO;

public interface NotificationsService {

	NotificationsResponseDTO createNotification(NotificationsRequestDTO dto);

	PagedResult<NotificationsResponseDTO> getAll(Pageable pageable);

	PagedResult<NotificationsResponseDTO> getByUserId(Integer userId, Pageable pageable);

	PagedResult<NotificationsResponseDTO> getUnreadByUserId(Integer userId, Pageable pageable);

	NotificationsResponseDTO findById(Integer id);

	NotificationsResponseDTO update(Integer id, NotificationsUpdateRequestDTO dto);

	NotificationsResponseDTO delete(Integer id);

	boolean hardDelete(Integer id);

	NotificationsResponseDTO markAsRead(Integer id);

	// Methods for current user (USER/DOCTOR)
	PagedResult<NotificationsResponseDTO> getByCurrentUser(String username, Pageable pageable);

	PagedResult<NotificationsResponseDTO> getUnreadByCurrentUser(String username, Pageable pageable);

	boolean markAsReadByCurrentUser(Integer id, String username);

	boolean softDeleteByCurrentUser(Integer id, String username);
}
