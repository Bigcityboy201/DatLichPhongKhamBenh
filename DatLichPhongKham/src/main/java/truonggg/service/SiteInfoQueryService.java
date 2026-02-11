package truonggg.service;

import org.springframework.data.domain.Pageable;

import truonggg.dto.reponseDTO.SiteInfoResponseDTO;
import truonggg.reponse.PagedResult;

public interface SiteInfoQueryService {

	PagedResult<SiteInfoResponseDTO> getAll(Pageable pageable);
}


