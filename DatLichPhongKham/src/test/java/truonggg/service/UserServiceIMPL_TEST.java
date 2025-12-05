package truonggg.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import truonggg.Model.User;
import truonggg.dto.reponseDTO.UserResponseDTO;
import truonggg.repo.UserRepository;
import truonggg.service.IMPL.UserServiceIMPL;

public class UserServiceIMPL_TEST {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserServiceIMPL userService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testGetUserById_Success() {
		User user = new User();
		user.setUserId(1);
		user.setUserName("user1");
		user.setEmail("user1@example.com");

		when(userRepository.findById(1)).thenReturn(Optional.of(user));

		UserResponseDTO result = userService.findById(1);

		assertEquals("user1", result.getUserName());
		assertEquals("user1@example.com", result.getEmail());
	}

	@Test
	void testGetAllUsers_Success() {
		User user1 = new User();
		user1.setUserId(1);
		user1.setUserName("user1");
		user1.setEmail("user1@example.com");

		User user2 = new User();
		user2.setUserId(2);
		user2.setUserName("user2");
		user2.setEmail("user2@example.com");

		when(userRepository.findAll()).thenReturn(List.of(user1, user2));

		List<UserResponseDTO> results = userService.getAll();

		assertEquals(2, results.size());
		assertEquals("user1", results.get(0).getUserName());
		assertEquals("user2", results.get(1).getUserName());
	}
}
