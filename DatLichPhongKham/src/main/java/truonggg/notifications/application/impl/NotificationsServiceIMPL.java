package truonggg.notifications.application.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import truonggg.Exception.NotFoundException;
import truonggg.notifications.domain.model.Notifications;
import truonggg.user.domain.model.User;
import truonggg.dto.reponseDTO.NotificationsResponseDTO;
import truonggg.dto.requestDTO.NotificationsRequestDTO;
import truonggg.dto.requestDTO.NotificationsUpdateRequestDTO;
import truonggg.notifications.mapper.NotificationsMapper;
import truonggg.user.infrastructure.UserRepository;
import truonggg.reponse.PagedResult;
import truonggg.notifications.application.NotificationsAdminService;
import truonggg.notifications.application.NotificationsSelfService;
import truonggg.notifications.infrastructure.NotificationsRepository;

@Service
@RequiredArgsConstructor
public class NotificationsServiceIMPL implements NotificationsAdminService, NotificationsSelfService {

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
		// notification.setIsRead(dto.getIsRead() != null ? dto.getIsRead() : false);

		notification = this.notificationsRepository.save(notification);
		NotificationsResponseDTO response = this.notificationsMapper.toDTO(notification);
		response.setIsRead(notification.getIsRead());
		// Đảm bảo userName được set đúng từ user đã load (luôn set để đảm bảo có giá trị)
		response.setUserName(user.getFullName());
		return response;
	}

	@Override
	public PagedResult<NotificationsResponseDTO> getAll(Pageable pageable) {
		Page<Notifications> notificationsPage = this.notificationsRepository.findAll(pageable);
		List<NotificationsResponseDTO> dtoList = notificationsPage.stream().map(notification -> {
			NotificationsResponseDTO dto = this.notificationsMapper.toDTO(notification);
			dto.setIsRead(notification.getIsRead());
			return dto;
		}).collect(Collectors.toList());

		return PagedResult.from(notificationsPage, dtoList);
	}

	@Override
	public PagedResult<NotificationsResponseDTO> getByUserId(Integer userId, Pageable pageable) {
		Page<Notifications> notificationsPage = this.notificationsRepository.findByUserUserId(userId, pageable);
		List<NotificationsResponseDTO> dtoList = notificationsPage.stream().map(notification -> {
			NotificationsResponseDTO dto = this.notificationsMapper.toDTO(notification);
			dto.setIsRead(notification.getIsRead());
			return dto;
		}).collect(Collectors.toList());

		return PagedResult.from(notificationsPage, dtoList);
	}

	@Override
	public PagedResult<NotificationsResponseDTO> getUnreadByUserId(Integer userId, Pageable pageable) {
		Page<Notifications> notificationsPage = this.notificationsRepository.findByUserUserIdAndIsRead(userId, false,
				pageable);
		List<NotificationsResponseDTO> dtoList = notificationsPage.stream().map(notification -> {
			NotificationsResponseDTO dto = this.notificationsMapper.toDTO(notification);
			dto.setIsRead(notification.getIsRead());
			return dto;
		}).collect(Collectors.toList());

		return PagedResult.from(notificationsPage, dtoList);
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
	public NotificationsResponseDTO update(Integer id, NotificationsUpdateRequestDTO dto) {
		Notifications foundNotification = this.notificationsRepository.findById(id)
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
	public NotificationsResponseDTO delete(Integer id) {
		Notifications foundNotification = this.notificationsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("notification", "Notification Not Found"));

		// Soft delete: xóa thực sự khỏi database (vì model không có field
		// deleted/isActive)
		this.notificationsRepository.delete(foundNotification);

		// Trả về DTO trước khi xóa
		NotificationsResponseDTO response = this.notificationsMapper.toDTO(foundNotification);
		response.setIsRead(foundNotification.getIsRead());
		return response;
	}

	@Override
	public boolean hardDelete(Integer id) {
		Notifications foundNotification = this.notificationsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("notification", "Notification Not Found"));

		this.notificationsRepository.delete(foundNotification);
		return true;
	}

	@Override
	public NotificationsResponseDTO markAsRead(Integer id) {
		Notifications foundNotification = this.notificationsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("notification", "Notification Not Found"));

		foundNotification.setIsRead(true);
		Notifications savedNotification = this.notificationsRepository.save(foundNotification);

		NotificationsResponseDTO response = this.notificationsMapper.toDTO(savedNotification);
		response.setIsRead(savedNotification.getIsRead());
		return response;
	}

	@Override
	public PagedResult<NotificationsResponseDTO> getByCurrentUser(String username, Pageable pageable) {
		User user = this.userRepository.findByUserName(username)
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		return this.getByUserId(user.getUserId(), pageable);
	}

	@Override
	public PagedResult<NotificationsResponseDTO> getUnreadByCurrentUser(String username, Pageable pageable) {
		User user = this.userRepository.findByUserName(username)
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		return this.getUnreadByUserId(user.getUserId(), pageable);
	}

	@Override
	public boolean markAsReadByCurrentUser(Integer id, String username) {
		User user = this.userRepository.findByUserName(username)
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		Notifications foundNotification = this.notificationsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("notification", "Notification Not Found"));

		// Kiểm tra quyền: user chỉ có thể đánh dấu đọc notification của chính mình
		if (!foundNotification.getUser().getUserId().equals(user.getUserId())) {
			throw new AccessDeniedException("You cannot modify this notification");
		}

		foundNotification.setIsRead(true);
		this.notificationsRepository.save(foundNotification);
		return true;
	}

	@Override
	public boolean softDeleteByCurrentUser(Integer id, String username) {
		User user = this.userRepository.findByUserName(username)
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		Notifications foundNotification = this.notificationsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("notification", "Notification Not Found"));

		// Kiểm tra quyền: user chỉ có thể xóa notification của chính mình
		if (!foundNotification.getUser().getUserId().equals(user.getUserId())) {
			throw new AccessDeniedException("You cannot modify this notification");
		}

		// Soft delete: Vì model không có field deleted/isActive,
		// ta sẽ xóa thực sự khỏi database (tương tự hard delete nhưng chỉ cho phép user
		// xóa của chính mình)
		this.notificationsRepository.delete(foundNotification);
		return true;
	}
}
