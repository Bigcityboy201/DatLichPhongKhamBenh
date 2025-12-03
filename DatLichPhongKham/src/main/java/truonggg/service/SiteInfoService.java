package truonggg.service;

import org.springframework.data.domain.Pageable;

import truonggg.reponse.PagedResult;
import truonggg.dto.reponseDTO.SiteInfoResponseDTO;
import truonggg.dto.requestDTO.SiteInfoDeleteRequestDTO;
import truonggg.dto.requestDTO.SiteInfoRequestDTO;
import truonggg.dto.requestDTO.SiteInfoUpdateRequestDTO;

public interface SiteInfoService {
	PagedResult<SiteInfoResponseDTO> getAll(Pageable pageable);

	SiteInfoResponseDTO save(final SiteInfoRequestDTO dto);

	SiteInfoResponseDTO update(SiteInfoUpdateRequestDTO dto);

	boolean delete(SiteInfoDeleteRequestDTO dto);

	boolean delete(Integer id);
}
