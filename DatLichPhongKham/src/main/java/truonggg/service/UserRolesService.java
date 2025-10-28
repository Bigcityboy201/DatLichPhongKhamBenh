package truonggg.service;

import java.util.List;

import truonggg.dto.reponseDTO.UserRolesResponseDTO;
import truonggg.dto.requestDTO.UserRolesDeleteRequestDTO;
import truonggg.dto.requestDTO.UserRolesRequestDTO;
import truonggg.dto.requestDTO.UserRolesUpdateRequestDTO;

public interface UserRolesService {
	UserRolesResponseDTO assignRole(UserRolesRequestDTO dto);

	List<UserRolesResponseDTO> getAll();

	List<UserRolesResponseDTO> getByUserId(Integer userId);

	List<UserRolesResponseDTO> getByRoleId(Integer roleId);

	List<UserRolesResponseDTO> getActiveByUserId(Integer userId);

	List<UserRolesResponseDTO> getActiveByRoleId(Integer roleId);

	UserRolesResponseDTO findById(Integer id);

	UserRolesResponseDTO update(UserRolesUpdateRequestDTO dto);

	boolean delete(UserRolesDeleteRequestDTO dto);

	boolean delete(Integer id);

	// Kiểm tra User có Role không
	boolean hasRole(Integer userId, Integer roleId);

	// Kiểm tra User có Role active không
	boolean hasActiveRole(Integer userId, Integer roleId);
}
