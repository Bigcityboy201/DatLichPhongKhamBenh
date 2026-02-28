package truonggg.notifications.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import truonggg.dto.reponseDTO.NotificationsResponseDTO;
import truonggg.dto.requestDTO.NotificationsRequestDTO;
import truonggg.dto.requestDTO.NotificationsUpdateRequestDTO;
import truonggg.reponse.PagedResult;
import truonggg.reponse.SuccessReponse;
import truonggg.notifications.application.NotificationsAdminService;
import truonggg.notifications.application.NotificationsSelfService;

@RestController
@RequestMapping(path = "/api/notifications")
@RequiredArgsConstructor
public class NotificationsController {

	private final NotificationsAdminService notificationsAdminService;

	private final NotificationsSelfService notificationsSelfService;

	// GET /api/notifications - Lấy tất cả notification, có phân trang
	@GetMapping
	@PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE')")
	public SuccessReponse<?> getAllNotifications(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<NotificationsResponseDTO> pagedResult = this.notificationsAdminService.getAll(pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// GET /api/notifications/{id} - Lấy notification theo ID
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE')")
	public SuccessReponse<NotificationsResponseDTO> getNotificationById(@PathVariable Integer id) {
		return SuccessReponse.of(this.notificationsAdminService.findById(id));
	}

	// GET /api/notifications/user/{userId} - Lấy theo User ID
	@PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE')")
	@GetMapping("/user/{userId}")
	public SuccessReponse<?> getNotificationsByUserId(@PathVariable Integer userId,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<NotificationsResponseDTO> pagedResult = this.notificationsAdminService.getByUserId(userId, pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// GET /api/notifications/user/{userId}/unread - Lấy chưa đọc
	@PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE')")
	@GetMapping("/user/{userId}/unread")
	public SuccessReponse<?> getUnreadNotificationsByUserId(@PathVariable Integer userId,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<NotificationsResponseDTO> pagedResult = this.notificationsAdminService.getUnreadByUserId(userId,
				pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// POST /api/notifications - Tạo notification mới cho bất kỳ user nào
	@PostMapping
	@PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE')")
	public SuccessReponse<NotificationsResponseDTO> createNotification(
			@RequestBody @Valid final NotificationsRequestDTO dto) {
		return SuccessReponse.of(this.notificationsAdminService.createNotification(dto));
	}

	// PUT /api/notifications/{id} - Cập nhật thông tin notification
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE')")
	public SuccessReponse<NotificationsResponseDTO> updateNotification(@PathVariable Integer id,
			@RequestBody @Valid NotificationsUpdateRequestDTO dto) {
		return SuccessReponse.of(this.notificationsAdminService.update(id, dto));
	}

	// PUT /api/notifications/{id}/read - Đánh dấu notification là đã đọc
	@PutMapping("/{id}/read")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE')")
	public SuccessReponse<NotificationsResponseDTO> markNotificationAsRead(@PathVariable Integer id) {
		return SuccessReponse.of(this.notificationsAdminService.markAsRead(id));
	}


	@GetMapping("/me")
	@PreAuthorize("hasAnyAuthority('USER', 'DOCTOR')")
	public SuccessReponse<?> getMyNotifications(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		PagedResult<NotificationsResponseDTO> pagedResult = this.notificationsSelfService.getByCurrentUser(username,
				pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// GET /api/notifications/me/unread - Lấy notification chưa đọc của chính mình
	@GetMapping("/me/unread")
	@PreAuthorize("hasAnyAuthority('USER', 'DOCTOR')")
	public SuccessReponse<?> getMyUnreadNotifications(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		PagedResult<NotificationsResponseDTO> pagedResult = this.notificationsSelfService.getUnreadByCurrentUser(username,
				pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// PUT /api/notifications/me/{id}/read - Đánh dấu notification của chính mình là
	// đã đọc
	@PutMapping("/me/{id}/read")
	@PreAuthorize("hasAnyAuthority('USER', 'DOCTOR')")
	public SuccessReponse<Boolean> markMyNotificationAsRead(@PathVariable Integer id) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return SuccessReponse.of(this.notificationsSelfService.markAsReadByCurrentUser(id, username));
	}

	// DELETE /api/notifications/me/{id} - Soft delete notification của chính mình
	@DeleteMapping("/me/{id}")
	@PreAuthorize("hasAnyAuthority('USER', 'DOCTOR')")
	public SuccessReponse<Boolean> softDeleteMyNotification(@PathVariable Integer id) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return SuccessReponse.of(this.notificationsSelfService.softDeleteByCurrentUser(id, username));
	}
}
