package truonggg.service.user;

import org.springframework.data.domain.Pageable;

import truonggg.dto.reponseDTO.UserResponseDTO;
import truonggg.dto.requestDTO.AssignRoleRequestDTO;
import truonggg.dto.requestDTO.UserRequestDTO;
import truonggg.dto.requestDTO.UserUpdateRequestDTO;
import truonggg.reponse.PagedResult;

public interface UserManagementService {

	UserResponseDTO createUser(UserRequestDTO dto);

	PagedResult<UserResponseDTO> getAllPaged(Pageable pageable);

	UserResponseDTO findById(Integer id);

	UserResponseDTO update(Integer id, UserUpdateRequestDTO dto);

	UserResponseDTO updateStatus(Integer id, Boolean isActive);

	boolean deleteManually(Integer id);

	UserResponseDTO assignRole(AssignRoleRequestDTO dto);

	UserResponseDTO findByUserName(String userName);
}
