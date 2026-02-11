package truonggg.service;

import org.springframework.data.domain.Pageable;

import truonggg.dto.reponseDTO.RoleResponseDTO;
import truonggg.reponse.PagedResult;

public interface RoleQueryService {

	PagedResult<RoleResponseDTO> getAll(Pageable pageable);

	RoleResponseDTO findById(Integer id);
}


