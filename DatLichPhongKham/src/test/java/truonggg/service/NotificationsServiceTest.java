package truonggg.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import truonggg.Exception.NotFoundException;
import truonggg.Model.Notifications;
import truonggg.Model.User;
import truonggg.dto.reponseDTO.NotificationsResponseDTO;
import truonggg.dto.requestDTO.NotificationsRequestDTO;
import truonggg.mapper.NotificationsMapper;
import truonggg.repo.NotificationsRepository;
import truonggg.repo.UserRepository;
import truonggg.reponse.PagedResult;
import truonggg.service.impl.NotificationsServiceIMPL;

@ExtendWith(MockitoExtension.class)
public class NotificationsServiceTest {

	@Mock
	private NotificationsRepository notificationsRepository;

	@Mock
	private NotificationsMapper notificationsMapper;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private NotificationsServiceIMPL notificationsService;

	@DisplayName("createNotification: success when user exists")
	@Test
	void createNotification_ShouldCreate_WhenUserExists() {
		NotificationsRequestDTO dto = new NotificationsRequestDTO();
		dto.setUserId(1);
		dto.setMessage("Hello");

		User user = new User();
		user.setUserId(1);
		user.setFullName("Test User");

		when(userRepository.findById(1)).thenReturn(Optional.of(user));
		when(notificationsRepository.save(any(Notifications.class))).thenReturn(new Notifications());
		when(notificationsMapper.toDTO(any(Notifications.class))).thenReturn(new NotificationsResponseDTO());

		NotificationsResponseDTO result = notificationsService.createNotification(dto);

		assertNotNull(result);
		verify(userRepository).findById(1);
		verify(notificationsRepository).save(any(Notifications.class));
	}

	@DisplayName("createNotification: throw NotFoundException when user not found")
	@Test
	void createNotification_ShouldThrow_WhenUserNotFound() {
		NotificationsRequestDTO dto = new NotificationsRequestDTO();
		dto.setUserId(1);

		when(userRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class,
				() -> notificationsService.createNotification(dto));
		assertEquals("user: User Not Found", ex.getMessage());

		verify(notificationsRepository, never()).save(any());
	}

	@DisplayName("getAll: success")
	@Test
	void getAll_ShouldReturnPagedResult() {
		Pageable pageable = PageRequest.of(0, 2);
		Notifications n1 = new Notifications();
		Notifications n2 = new Notifications();

		Page<Notifications> page = new PageImpl<>(List.of(n1, n2), pageable, 2);

		when(notificationsRepository.findAll(pageable)).thenReturn(page);
		when(notificationsMapper.toDTO(any(Notifications.class))).thenReturn(new NotificationsResponseDTO());

		PagedResult<NotificationsResponseDTO> result = notificationsService.getAll(pageable);

		assertNotNull(result);
		assertEquals(2, result.getContent().size());
		verify(notificationsRepository).findAll(pageable);
	}

	@DisplayName("markAsReadByCurrentUser: success when owner")
	@Test
	void markAsReadByCurrentUser_ShouldSucceed_WhenOwner() {
		Integer id = 1;
		String username = "user1";

		User user = new User();
		user.setUserId(1);
		user.setUserName(username);

		Notifications notification = new Notifications();
		notification.setUser(user);

		when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
		when(notificationsRepository.findById(id)).thenReturn(Optional.of(notification));

		boolean result = notificationsService.markAsReadByCurrentUser(id, username);

		assertTrue(result);
		verify(notificationsRepository).save(notification);
	}

	@DisplayName("markAsReadByCurrentUser: throw AccessDeniedException when not owner")
	@Test
	void markAsReadByCurrentUser_ShouldThrow_WhenNotOwner() {
		Integer id = 1;
		String username = "user1";

		User user = new User();
		user.setUserId(1);
		user.setUserName(username);

		User otherUser = new User();
		otherUser.setUserId(2);

		Notifications notification = new Notifications();
		notification.setUser(otherUser);

		when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
		when(notificationsRepository.findById(id)).thenReturn(Optional.of(notification));

		AccessDeniedException ex = assertThrows(AccessDeniedException.class,
				() -> notificationsService.markAsReadByCurrentUser(id, username));
		assertEquals("You cannot modify this notification", ex.getMessage());
	}
}


