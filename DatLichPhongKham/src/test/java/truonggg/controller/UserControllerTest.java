package truonggg.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.SimpleDateFormat;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import truonggg.dto.reponseDTO.UserResponseDTO;
import truonggg.dto.requestDTO.UserRequestDTO;
import truonggg.dto.requestDTO.UserStatusDTO;
import truonggg.dto.requestDTO.UserUpdateRequestDTO;
import truonggg.reponse.PagedResult;
import truonggg.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@Autowired
	private ObjectMapper objectMapper;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	@Test
	@WithMockUser(username = "admin", authorities = { "ADMIN" })
	public void testCreateUser_Success() throws Exception {
		UserRequestDTO requestDTO = new UserRequestDTO();
		requestDTO.setUserName("testuser");
		requestDTO.setEmail("test@example.com");
		requestDTO.setPassword("123456");
		requestDTO.setFullName("Test User");
		requestDTO.setPhone("0123456789");
		requestDTO.setDateOfBirth(sdf.parse("2000-01-01"));

		UserResponseDTO responseDTO = UserResponseDTO.builder().userName("testuser").email("test@example.com")
				.fullName("Test User").phone("0123456789").build();

		when(userService.createUser(any(UserRequestDTO.class))).thenReturn(responseDTO);

		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDTO)).with(csrf())).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.userName").value("testuser"))
				.andExpect(jsonPath("$.data.email").value("test@example.com"))
				.andExpect(jsonPath("$.data.fullName").value("Test User"))
				.andExpect(jsonPath("$.data.phone").value("0123456789"));
	}

	@Test
	@WithMockUser(username = "admin", authorities = { "ADMIN" })
	public void testUpdateUser_Success() throws Exception {
		UserUpdateRequestDTO requestDTO = new UserUpdateRequestDTO();
		requestDTO.setFullName("Updated User");
		requestDTO.setEmail("updated@example.com");

		UserResponseDTO responseDTO = UserResponseDTO.builder().userName("testuser").email("updated@example.com")
				.fullName("Updated User").phone("0123456789").build();

		when(userService.update(eq(1), any(UserUpdateRequestDTO.class))).thenReturn(responseDTO);

		mockMvc.perform(patch("/api/users/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDTO)).with(csrf())).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.fullName").value("Updated User"))
				.andExpect(jsonPath("$.data.email").value("updated@example.com"));
	}

	@Test
	@WithMockUser(username = "admin", authorities = { "ADMIN" })
	public void testUpdateUserStatus_Success() throws Exception {
		UserStatusDTO statusDTO = new UserStatusDTO();
		statusDTO.setActive(true);

		UserResponseDTO responseDTO = UserResponseDTO.builder().userName("testuser").email("test@example.com")
				.fullName("Test User").phone("0123456789").build();

		when(userService.updateStatus(eq(1), eq(true))).thenReturn(responseDTO);

		mockMvc.perform(patch("/api/users/1/status").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(statusDTO)).with(csrf())).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.userName").value("testuser"));
	}

	@Test
	@WithMockUser(username = "admin", authorities = { "ADMIN" })
	public void testDeleteUser_Success() throws Exception {
		when(userService.deleteManually(1)).thenReturn(true);

		mockMvc.perform(delete("/api/users/manually/1").with(csrf())).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "admin", authorities = { "ADMIN" })
	public void testGetUserById_Success() throws Exception {
		UserResponseDTO responseDTO = UserResponseDTO.builder().userName("testuser").email("test@example.com")
				.fullName("Test User").phone("0123456789").build();

		when(userService.findById(1)).thenReturn(responseDTO);

		mockMvc.perform(get("/api/users/1").with(csrf())).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.userName").value("testuser"))
				.andExpect(jsonPath("$.data.email").value("test@example.com"));
	}

	@Test
	@WithMockUser(username = "admin", authorities = { "ADMIN" })
	public void testGetAllUsers_Success() throws Exception {
		List<UserResponseDTO> users = List.of(
				UserResponseDTO.builder().userName("user1").email("user1@example.com").fullName("User One").build(),
				UserResponseDTO.builder().userName("user2").email("user2@example.com").fullName("User Two").build());

		PagedResult<UserResponseDTO> pagedResult = PagedResult.<UserResponseDTO>builder().content(users).currentPage(0)
				.pageSize(10).totalElements(users.size()).totalPages(1).build();

		when(userService.getAllPaged(any())).thenReturn(pagedResult);

		mockMvc.perform(get("/api/users").with(csrf())).andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].userName").value("user1"))
				.andExpect(jsonPath("$.data[1].userName").value("user2"));

	}
}
