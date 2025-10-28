package truonggg.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import truonggg.dto.reponseDTO.SiteInfoResponseDTO;
import truonggg.dto.requestDTO.SiteInfoDeleteRequestDTO;
import truonggg.dto.requestDTO.SiteInfoRequestDTO;
import truonggg.dto.requestDTO.SiteInfoUpdateRequestDTO;
import truonggg.reponse.SuccessReponse;
import truonggg.service.SiteInfoService;

@RestController
@RequestMapping(path = "/api/siteinfos")
@RequiredArgsConstructor
public class SiteInfoController {
	private final SiteInfoService siteInfoService;

	// GET /api/siteinfos - Lấy tất cả
	@GetMapping
	public SuccessReponse<List<SiteInfoResponseDTO>> getAllSiteInfos() {
		return SuccessReponse.of(this.siteInfoService.getAll());
	}

	// POST /api/siteinfos - Tạo mới
	@PostMapping
	public SuccessReponse<SiteInfoResponseDTO> createSiteInfo(@RequestBody @Valid final SiteInfoRequestDTO dto) {
		return SuccessReponse.of(this.siteInfoService.save(dto));
	}

	// PUT /api/siteinfos - Cập nhật
	@PutMapping
	public SuccessReponse<SiteInfoResponseDTO> updateSiteInfo(@RequestBody @Valid SiteInfoUpdateRequestDTO dto) {
		return SuccessReponse.of(this.siteInfoService.update(dto));
	}

	// DELETE /api/siteinfos - Soft delete
	@DeleteMapping
	public SuccessReponse<Boolean> deleteSiteInfo(@RequestBody @Valid SiteInfoDeleteRequestDTO dto) {
		return SuccessReponse.of(this.siteInfoService.delete(dto));
	}

	// DELETE /api/siteinfos/{id} - Hard delete
	@DeleteMapping("/{id}")
	public SuccessReponse<Boolean> hardDeleteSiteInfo(@PathVariable Integer id) {
		return SuccessReponse.of(this.siteInfoService.delete(id));
	}
}
