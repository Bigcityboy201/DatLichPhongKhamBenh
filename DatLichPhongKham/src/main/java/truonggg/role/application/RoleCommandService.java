package truonggg.role.application;

import truonggg.dto.reponseDTO.RoleResponseDTO;
import truonggg.dto.requestDTO.RoleDeleteRequestDTO;
import truonggg.dto.requestDTO.RoleRequestDTO;
import truonggg.dto.requestDTO.RoleUpdateRequestDTO;

public interface RoleCommandService {

	RoleResponseDTO createRole(RoleRequestDTO dto);

	RoleResponseDTO update(RoleUpdateRequestDTO dto);

	boolean delete(RoleDeleteRequestDTO dto);

	boolean delete(Integer id);
}


