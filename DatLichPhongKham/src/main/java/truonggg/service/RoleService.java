package truonggg.service;

import org.springframework.data.domain.Pageable;

import truonggg.reponse.PagedResult;
import truonggg.dto.reponseDTO.RoleResponseDTO;
import truonggg.dto.requestDTO.RoleDeleteRequestDTO;
import truonggg.dto.requestDTO.RoleRequestDTO;
import truonggg.dto.requestDTO.RoleUpdateRequestDTO;

public interface RoleService {
	RoleResponseDTO createRole(RoleRequestDTO dto);

	PagedResult<RoleResponseDTO> getAll(Pageable pageable);

	RoleResponseDTO findById(Integer id);

	RoleResponseDTO update(RoleUpdateRequestDTO dto);

	boolean delete(RoleDeleteRequestDTO dto);

	boolean delete(Integer id);
}
