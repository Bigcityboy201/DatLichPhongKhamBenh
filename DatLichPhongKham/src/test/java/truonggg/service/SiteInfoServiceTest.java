package truonggg.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import truonggg.Exception.NotFoundException;
import truonggg.siteInfo.domain.model.SiteInfo;
import truonggg.dto.reponseDTO.SiteInfoResponseDTO;
import truonggg.dto.requestDTO.SiteInfoDeleteRequestDTO;
import truonggg.dto.requestDTO.SiteInfoRequestDTO;
import truonggg.dto.requestDTO.SiteInfoUpdateRequestDTO;
import truonggg.siteInfo.mapper.SiteInfoMapper;
import truonggg.siteInfo.infrastructure.SiteInfoRepository;
import truonggg.reponse.PagedResult;
import truonggg.siteInfo.application.impl.SiteInfoServiceIMPL;

@ExtendWith(MockitoExtension.class)
public class SiteInfoServiceTest {

	@Mock
	private SiteInfoRepository siteInfoRepository;

	@Mock
	private SiteInfoMapper siteInfoMapper;

	@InjectMocks
	private SiteInfoServiceIMPL siteInfoService;

	@DisplayName("getAll: success")
	@Test
	void getAll_ShouldReturnPagedResult() {
		Pageable pageable = PageRequest.of(0, 2);
		SiteInfo s1 = new SiteInfo();
		SiteInfo s2 = new SiteInfo();

		Page<SiteInfo> page = new PageImpl<>(List.of(s1, s2), pageable, 2);

		when(siteInfoRepository.findAll(pageable)).thenReturn(page);
		when(siteInfoMapper.toDTO(any(SiteInfo.class))).thenReturn(new SiteInfoResponseDTO());

		PagedResult<SiteInfoResponseDTO> result = siteInfoService.getAll(pageable);

		assertNotNull(result);
		assertEquals(2, result.getContent().size());
		verify(siteInfoRepository).findAll(pageable);
	}

	@DisplayName("save: success")
	@Test
	void save_ShouldCreateSiteInfo() {
		SiteInfoRequestDTO dto = new SiteInfoRequestDTO();
		dto.setInfoKey("SITE_NAME");
		dto.setValue("Clinic A");
		SiteInfo saved = new SiteInfo();

		when(siteInfoRepository.save(any(SiteInfo.class))).thenReturn(saved);
		when(siteInfoMapper.toDTO(saved)).thenReturn(new SiteInfoResponseDTO());

		SiteInfoResponseDTO result = siteInfoService.save(dto);

		assertNotNull(result);
		verify(siteInfoRepository).save(any(SiteInfo.class));
	}

	@DisplayName("update: throw NotFoundException when siteInfo not found")
	@Test
	void update_ShouldThrow_WhenNotFound() {
		SiteInfoUpdateRequestDTO dto = new SiteInfoUpdateRequestDTO();
		dto.setId(1);

		when(siteInfoRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> siteInfoService.update(dto));
		assertEquals("siteInfo: SiteInfo Not Found", ex.getMessage());
	}

	@DisplayName("delete (soft): success when exists")
	@Test
	void deleteSoft_ShouldSucceed_WhenExists() {
		SiteInfoDeleteRequestDTO dto = new SiteInfoDeleteRequestDTO();
		dto.setId(1);
		dto.setIsActive(false);

		SiteInfo found = new SiteInfo();

		when(siteInfoRepository.findById(1)).thenReturn(Optional.of(found));
		when(siteInfoRepository.save(found)).thenReturn(found);

		boolean result = siteInfoService.delete(dto);

		assertTrue(result);
		assertEquals(false, found.getIsActive());
		verify(siteInfoRepository).save(found);
	}

	@DisplayName("delete (hard): throw NotFoundException when not found")
	@Test
	void deleteHard_ShouldThrow_WhenNotFound() {
		when(siteInfoRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> siteInfoService.delete(1));
		assertEquals("siteInfo: SiteInfo Not Found", ex.getMessage());

		verify(siteInfoRepository, never()).delete(any());
	}
}


