package truonggg.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import truonggg.dto.reponseDTO.NotificationsResponseDTO;
import truonggg.dto.requestDTO.NotificationsDeleteRequestDTO;
import truonggg.dto.requestDTO.NotificationsRequestDTO;
import truonggg.dto.requestDTO.NotificationsUpdateRequestDTO;
import truonggg.reponse.SuccessReponse;
import truonggg.service.NotificationsService;

@RestController
@RequestMapping(path = "/api/notifications")
@RequiredArgsConstructor
public class NotificationsController {

	private final NotificationsService notificationsService;

	// GET /api/notifications - Lấy tất cả
	@GetMapping
	public SuccessReponse<List<NotificationsResponseDTO>> getAllNotifications() {
		return SuccessReponse.of(this.notificationsService.getAll());
	}

	// GET /api/notifications/{id} - Lấy theo ID
	@GetMapping("/{id}")
	public SuccessReponse<NotificationsResponseDTO> getNotificationById(@PathVariable Integer id) {
		return SuccessReponse.of(this.notificationsService.findById(id));
	}

	// GET /api/notifications/user/{userId} - Lấy theo User ID
	@GetMapping("/user/{userId}")
	public SuccessReponse<List<NotificationsResponseDTO>> getNotificationsByUserId(@PathVariable Integer userId) {
		return SuccessReponse.of(this.notificationsService.getByUserId(userId));
	}

	// GET /api/notifications/user/{userId}/unread - Lấy chưa đọc
	@GetMapping("/user/{userId}/unread")
	public SuccessReponse<List<NotificationsResponseDTO>> getUnreadNotificationsByUserId(@PathVariable Integer userId) {
		return SuccessReponse.of(this.notificationsService.getUnreadByUserId(userId));
	}

	// POST /api/notifications - Tạo mới
	@PostMapping
	public SuccessReponse<NotificationsResponseDTO> createNotification(@RequestBody @Valid final NotificationsRequestDTO dto) {
		return SuccessReponse.of(this.notificationsService.createNotification(dto));
	}

	// PUT /api/notifications - Cập nhật
	@PutMapping
	public SuccessReponse<NotificationsResponseDTO> updateNotification(@RequestBody @Valid NotificationsUpdateRequestDTO dto) {
		return SuccessReponse.of(this.notificationsService.update(dto));
	}

	// PUT /api/notifications/{id}/read - Đánh dấu đã đọc
	@PutMapping("/{id}/read")
	public SuccessReponse<Boolean> markNotificationAsRead(@PathVariable Integer id) {
		return SuccessReponse.of(this.notificationsService.markAsRead(id));
	}

	// DELETE /api/notifications - Soft delete
	@DeleteMapping
	public SuccessReponse<Boolean> deleteNotification(@RequestBody @Valid NotificationsDeleteRequestDTO dto) {
		return SuccessReponse.of(this.notificationsService.delete(dto));
	}

	// DELETE /api/notifications/{id} - Hard delete
	@DeleteMapping("/{id}")
	public SuccessReponse<Boolean> hardDeleteNotification(@PathVariable Integer id) {
		return SuccessReponse.of(this.notificationsService.delete(id));
	}
}

