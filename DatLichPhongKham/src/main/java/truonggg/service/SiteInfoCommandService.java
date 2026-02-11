package truonggg.service;

import truonggg.dto.reponseDTO.SiteInfoResponseDTO;
import truonggg.dto.requestDTO.SiteInfoDeleteRequestDTO;
import truonggg.dto.requestDTO.SiteInfoRequestDTO;
import truonggg.dto.requestDTO.SiteInfoUpdateRequestDTO;

public interface SiteInfoCommandService {

	SiteInfoResponseDTO save(final SiteInfoRequestDTO dto);

	SiteInfoResponseDTO update(SiteInfoUpdateRequestDTO dto);

	boolean delete(SiteInfoDeleteRequestDTO dto);

	boolean delete(Integer id);
}


