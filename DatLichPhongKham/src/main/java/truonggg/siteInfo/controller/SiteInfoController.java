package truonggg.siteInfo.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import truonggg.dto.reponseDTO.SiteInfoResponseDTO;
import truonggg.dto.requestDTO.SiteInfoDeleteRequestDTO;
import truonggg.dto.requestDTO.SiteInfoRequestDTO;
import truonggg.dto.requestDTO.SiteInfoUpdateRequestDTO;
import truonggg.reponse.PagedResult;
import truonggg.reponse.SuccessReponse;
import truonggg.siteInfo.application.SiteInfoCommandService;
import truonggg.siteInfo.application.SiteInfoQueryService;

@RestController
@RequestMapping(path = "/api/siteinfos")
@RequiredArgsConstructor
public class SiteInfoController {
	private final SiteInfoQueryService siteInfoQueryService;
	private final SiteInfoCommandService siteInfoCommandService;

	// GET /api/siteinfos - Lấy tất cả
	@GetMapping
	public SuccessReponse<?> getAllSiteInfos(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<SiteInfoResponseDTO> pagedResult = this.siteInfoQueryService.getAll(pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// POST /api/siteinfos - Tạo mới
	@PostMapping
	@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
	public SuccessReponse<SiteInfoResponseDTO> createSiteInfo(@RequestBody @Valid final SiteInfoRequestDTO dto) {
		return SuccessReponse.of(this.siteInfoCommandService.save(dto));
	}

	// PUT /api/siteinfos - Cập nhật
	@PutMapping
	@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
	public SuccessReponse<SiteInfoResponseDTO> updateSiteInfo(@RequestBody @Valid SiteInfoUpdateRequestDTO dto) {
		return SuccessReponse.of(this.siteInfoCommandService.update(dto));
	}

	// DELETE /api/siteinfos - Soft delete
	@DeleteMapping
	@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
	public SuccessReponse<Boolean> deleteSiteInfo(@RequestBody @Valid SiteInfoDeleteRequestDTO dto) {
		return SuccessReponse.of(this.siteInfoCommandService.delete(dto));
	}

	// DELETE /api/siteinfos/{id} - Hard delete
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
	public SuccessReponse<Boolean> hardDeleteSiteInfo(@PathVariable Integer id) {
		return SuccessReponse.of(this.siteInfoCommandService.delete(id));
	}
}
