package truonggg.service;

import java.util.List;

import truonggg.Model.User;
import truonggg.dto.reponseDTO.UserResponseDTO;
import truonggg.dto.requestDTO.AssignRoleRequestDTO;
import truonggg.dto.requestDTO.UserDeleteRequestDTO;
import truonggg.dto.requestDTO.UserRequestDTO;
import truonggg.dto.requestDTO.UserUpdateRequestDTO;

public interface UserService {
	UserResponseDTO createUser(UserRequestDTO dto);

	Boolean signUp(final User user);

	List<UserResponseDTO> getAll();

	UserResponseDTO findById(Integer id);

	UserResponseDTO update(UserUpdateRequestDTO dto);

	boolean delete(UserDeleteRequestDTO dto);

	boolean delete(Integer id);
	
	Boolean assignRole(AssignRoleRequestDTO dto);

	User update(User user);
}
