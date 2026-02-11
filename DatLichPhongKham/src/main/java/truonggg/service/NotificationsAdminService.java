package truonggg.service;

import org.springframework.data.domain.Pageable;

import truonggg.dto.reponseDTO.NotificationsResponseDTO;
import truonggg.dto.requestDTO.NotificationsRequestDTO;
import truonggg.dto.requestDTO.NotificationsUpdateRequestDTO;
import truonggg.reponse.PagedResult;

public interface NotificationsAdminService {

	NotificationsResponseDTO createNotification(NotificationsRequestDTO dto);

	PagedResult<NotificationsResponseDTO> getAll(Pageable pageable);

	PagedResult<NotificationsResponseDTO> getByUserId(Integer userId, Pageable pageable);

	PagedResult<NotificationsResponseDTO> getUnreadByUserId(Integer userId, Pageable pageable);

	NotificationsResponseDTO findById(Integer id);

	NotificationsResponseDTO update(Integer id, NotificationsUpdateRequestDTO dto);

	NotificationsResponseDTO delete(Integer id);

	boolean hardDelete(Integer id);

	NotificationsResponseDTO markAsRead(Integer id);
}


