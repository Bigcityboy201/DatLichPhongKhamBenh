package truonggg.service;

import java.util.List;

import truonggg.dto.reponseDTO.NotificationsResponseDTO;
import truonggg.dto.requestDTO.NotificationsDeleteRequestDTO;
import truonggg.dto.requestDTO.NotificationsRequestDTO;
import truonggg.dto.requestDTO.NotificationsUpdateRequestDTO;

public interface NotificationsService {

	NotificationsResponseDTO createNotification(NotificationsRequestDTO dto);

	List<NotificationsResponseDTO> getAll();

	List<NotificationsResponseDTO> getByUserId(Integer userId);

	List<NotificationsResponseDTO> getUnreadByUserId(Integer userId);

	NotificationsResponseDTO findById(Integer id);

	NotificationsResponseDTO update(NotificationsUpdateRequestDTO dto);

	boolean delete(NotificationsDeleteRequestDTO dto);

	boolean delete(Integer id);

	boolean markAsRead(Integer id);
}
