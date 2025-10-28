package truonggg.service;

import java.util.List;

import truonggg.dto.reponseDTO.RoleResponseDTO;
import truonggg.dto.requestDTO.RoleDeleteRequestDTO;
import truonggg.dto.requestDTO.RoleRequestDTO;
import truonggg.dto.requestDTO.RoleUpdateRequestDTO;

public interface RoleService {
	RoleResponseDTO createRole(RoleRequestDTO dto);

	List<RoleResponseDTO> getAll();

	RoleResponseDTO findById(Integer id);

	RoleResponseDTO update(RoleUpdateRequestDTO dto);

	boolean delete(RoleDeleteRequestDTO dto);

	boolean delete(Integer id);
}
