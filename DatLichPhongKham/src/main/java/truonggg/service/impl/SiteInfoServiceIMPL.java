package truonggg.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import truonggg.Exception.NotFoundException;
import truonggg.Model.SiteInfo;
import truonggg.dto.reponseDTO.SiteInfoResponseDTO;
import truonggg.dto.requestDTO.SiteInfoDeleteRequestDTO;
import truonggg.dto.requestDTO.SiteInfoRequestDTO;
import truonggg.dto.requestDTO.SiteInfoUpdateRequestDTO;
import truonggg.mapper.SiteInfoMapper;
import truonggg.repo.SiteInfoRepository;
import truonggg.reponse.PagedResult;
import truonggg.service.SiteInfoCommandService;
import truonggg.service.SiteInfoQueryService;

@Service
@RequiredArgsConstructor
public class SiteInfoServiceIMPL implements SiteInfoQueryService, SiteInfoCommandService {
	private final SiteInfoRepository siteInfoRepository;
	private final SiteInfoMapper siteInfoMapper;

	@Override
	public PagedResult<SiteInfoResponseDTO> getAll(Pageable pageable) {
		Page<SiteInfo> siteInfoPage = this.siteInfoRepository.findAll(pageable);
		List<SiteInfoResponseDTO> dtoList = siteInfoPage.stream()
				.map(siteInfoMapper::toDTO)
				.collect(Collectors.toList());
		
		return PagedResult.from(siteInfoPage, dtoList);
	}

	@Override
	public SiteInfoResponseDTO save(SiteInfoRequestDTO dto) {
		SiteInfo siteInfo = this.siteInfoMapper.toModel(dto);
		return this.siteInfoMapper.toDTO(this.siteInfoRepository.save(siteInfo));
	}

	@Override
	public SiteInfoResponseDTO update(SiteInfoUpdateRequestDTO dto) {
		SiteInfo foundSiteInfo = this.siteInfoRepository.findById(dto.getId())
				.orElseThrow(() -> new NotFoundException("siteInfo", "SiteInfo Not Found"));

		// Cập nhật infoKey nếu có
		if (dto.getInfoKey() != null) {
			foundSiteInfo.setInfoKey(dto.getInfoKey());
		}

		// Cập nhật value nếu có
		if (dto.getValue() != null) {
			foundSiteInfo.setValue(dto.getValue());
		}

		// Cập nhật updatedAt
		foundSiteInfo.setUpdatedAt(LocalDateTime.now());

		return this.siteInfoMapper.toDTO(this.siteInfoRepository.save(foundSiteInfo));
	}

	@Override
	public boolean delete(SiteInfoDeleteRequestDTO dto) {
		SiteInfo foundSiteInfo = this.siteInfoRepository.findById(dto.getId())
				.orElseThrow(() -> new NotFoundException("siteInfo", "SiteInfo Not Found"));

		if (dto.getIsActive() != null) {
			foundSiteInfo.setIsActive(dto.getIsActive());
			foundSiteInfo.setUpdatedAt(LocalDateTime.now());
			this.siteInfoRepository.save(foundSiteInfo);
		}
		return true;
	}

	@Override
	public boolean delete(Integer id) {
		SiteInfo foundSiteInfo = this.siteInfoRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("siteInfo", "SiteInfo Not Found"));

		this.siteInfoRepository.delete(foundSiteInfo);
		return true;
	}

}
