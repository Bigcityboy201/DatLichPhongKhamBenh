package truonggg.service.IMPL;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import truonggg.Exception.NotFoundException;
import truonggg.Model.Notifications;
import truonggg.Model.User;
import truonggg.dto.reponseDTO.NotificationsResponseDTO;
import truonggg.dto.requestDTO.NotificationsDeleteRequestDTO;
import truonggg.dto.requestDTO.NotificationsRequestDTO;
import truonggg.dto.requestDTO.NotificationsUpdateRequestDTO;
import truonggg.mapper.NotificationsMapper;
import truonggg.repo.NotificationsRepository;
import truonggg.repo.UserRepository;
import truonggg.service.NotificationsService;

@Service
@RequiredArgsConstructor
public class NotificationsServiceIMPL implements NotificationsService {

	private final NotificationsRepository notificationsRepository;
	private final NotificationsMapper notificationsMapper;
	private final UserRepository userRepository;

	@Override
	public NotificationsResponseDTO createNotification(NotificationsRequestDTO dto) {
		User user = this.userRepository.findById(dto.getUserId())
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		Notifications notification = new Notifications();
		notification.setMessage(dto.getMessage());
		notification.setUser(user);
		notification.setCreatedAt(new Date());
		notification.setIsRead(dto.getIsRead() != null ? dto.getIsRead() : false);

		notification = this.notificationsRepository.save(notification);
		NotificationsResponseDTO response = this.notificationsMapper.toDTO(notification);
		response.setIsRead(notification.getIsRead());
		return response;
	}

	@Override
	public List<NotificationsResponseDTO> getAll() {
		List<Notifications> notifications = this.notificationsRepository.findAll();
		return notifications.stream().map(notification -> {
			NotificationsResponseDTO dto = this.notificationsMapper.toDTO(notification);
			dto.setIsRead(notification.getIsRead());
			return dto;
		}).toList();
	}

	@Override
	public List<NotificationsResponseDTO> getByUserId(Integer userId) {
		List<Notifications> notifications = this.notificationsRepository.findByUserUserId(userId);
		return notifications.stream().map(notification -> {
			NotificationsResponseDTO dto = this.notificationsMapper.toDTO(notification);
			dto.setIsRead(notification.getIsRead());
			return dto;
		}).toList();
	}

	@Override
	public List<NotificationsResponseDTO> getUnreadByUserId(Integer userId) {
		List<Notifications> notifications = this.notificationsRepository.findByUserUserIdAndIsRead(userId, false);
		return notifications.stream().map(notification -> {
			NotificationsResponseDTO dto = this.notificationsMapper.toDTO(notification);
			dto.setIsRead(notification.getIsRead());
			return dto;
		}).toList();
	}

	@Override
	public NotificationsResponseDTO findById(Integer id) {
		Notifications notification = this.notificationsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("notification", "Notification Not Found"));
		NotificationsResponseDTO dto = this.notificationsMapper.toDTO(notification);
		dto.setIsRead(notification.getIsRead());
		return dto;
	}

	@Override
	public NotificationsResponseDTO update(NotificationsUpdateRequestDTO dto) {
		Notifications foundNotification = this.notificationsRepository.findById(dto.getId())
				.orElseThrow(() -> new NotFoundException("notification", "Notification Not Found"));

		// Cập nhật message nếu có
		if (dto.getMessage() != null) {
			foundNotification.setMessage(dto.getMessage());
		}

		// Cập nhật user nếu có
		if (dto.getUserId() != null) {
			User user = this.userRepository.findById(dto.getUserId())
					.orElseThrow(() -> new NotFoundException("user", "User Not Found"));
			foundNotification.setUser(user);
		}

		// Cập nhật isRead nếu có
		if (dto.getIsRead() != null) {
			foundNotification.setIsRead(dto.getIsRead());
		}

		Notifications savedNotification = this.notificationsRepository.save(foundNotification);
		NotificationsResponseDTO response = this.notificationsMapper.toDTO(savedNotification);
		response.setIsRead(savedNotification.getIsRead());
		return response;
	}

	@Override
	public boolean delete(NotificationsDeleteRequestDTO dto) {
		Notifications foundNotification = this.notificationsRepository.findById(dto.getId())
				.orElseThrow(() -> new NotFoundException("notification", "Notification Not Found"));

		if (dto.getIsRead() != null) {
			foundNotification.setIsRead(dto.getIsRead());
			this.notificationsRepository.save(foundNotification);
		}
		return true;
	}

	@Override
	public boolean delete(Integer id) {
		Notifications foundNotification = this.notificationsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("notification", "Notification Not Found"));

		this.notificationsRepository.delete(foundNotification);
		return true;
	}

	@Override
	public boolean markAsRead(Integer id) {
		Notifications foundNotification = this.notificationsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("notification", "Notification Not Found"));

		foundNotification.setIsRead(true);
		this.notificationsRepository.save(foundNotification);
		return true;
	}
}
