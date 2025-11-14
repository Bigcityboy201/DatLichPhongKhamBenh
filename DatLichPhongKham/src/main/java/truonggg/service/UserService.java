package truonggg.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import truonggg.Model.User;
import truonggg.dto.reponseDTO.UserResponseDTO;
import truonggg.dto.requestDTO.AssignRoleRequestDTO;
import truonggg.dto.requestDTO.UserRequestDTO;
import truonggg.dto.requestDTO.UserUpdateRequestDTO;
import truonggg.reponse.PagedResult;

public interface UserService {
	UserResponseDTO createUser(UserRequestDTO dto);

	UserResponseDTO signUp(final User user);

	List<UserResponseDTO> getAll();

	PagedResult<UserResponseDTO> getAllPaged(Pageable pageable);

	UserResponseDTO findById(Integer id);

	User update(User user);

	UserResponseDTO update(Integer id, UserUpdateRequestDTO dto);

	UserResponseDTO updateStatus(Integer id, Boolean isActive);

	boolean deleteManually(Integer id);

	Boolean assignRole(AssignRoleRequestDTO dto);
}
