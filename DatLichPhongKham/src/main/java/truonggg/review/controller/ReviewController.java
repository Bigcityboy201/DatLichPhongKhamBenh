package truonggg.review.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
import truonggg.dto.reponseDTO.ReviewResponseDTO;
import truonggg.dto.requestDTO.ReviewSelfRequestDTO;
import truonggg.dto.requestDTO.ReviewUpdateRequestDTO;
import truonggg.dto.requestDTO.ReviewUpdateRequestDTO_USER;
import truonggg.reponse.PagedResult;
import truonggg.reponse.SuccessReponse;
import truonggg.review.application.ReviewAdminService;
import truonggg.review.application.ReviewSelfService;

@RestController
@RequestMapping(path = "/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewAdminService reviewAdminService;

	private final ReviewSelfService reviewSelfService;

	// GET /api/reviews - Lấy tất cả
	@GetMapping
	@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
	public SuccessReponse<?> getAllReviews(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<ReviewResponseDTO> pagedResult = this.reviewAdminService.getAll(pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// GET /api/reviews/{id} - Lấy theo ID
	@GetMapping("/{id}")
	public SuccessReponse<ReviewResponseDTO> getReviewById(@PathVariable Integer id) {
		return SuccessReponse.of(this.reviewAdminService.findById(id));
	}

	// GET /api/reviews/doctor/{doctorId} - Lấy theo Doctor ID
	@GetMapping("/doctor/{doctorId}")
	public SuccessReponse<?> getReviewsByDoctorId(@PathVariable Integer doctorId,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<ReviewResponseDTO> pagedResult = this.reviewAdminService.getByDoctorId(doctorId, pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

//	// POST /api/reviews - Tạo mới
//	@PostMapping
//	public SuccessReponse<ReviewResponseDTO> createReview(@RequestBody @Valid final ReviewRequestDTO dto) {
//		return SuccessReponse.of(this.reviewService.createReview(dto));
//	}

	// GET /api/reviews/me - Lấy review của user hiện tại
	@GetMapping("/me")
	@PreAuthorize("hasAnyAuthority('USER', 'DOCTOR', 'EMPLOYEE', 'ADMIN')")
	public SuccessReponse<?> getMyReviews(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		PagedResult<ReviewResponseDTO> pagedResult = this.reviewSelfService.getByCurrentUser(username, pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// POST /api/reviews/me - User tạo review cho chính mình
	@PostMapping("/me")
	@PreAuthorize("hasAnyAuthority('USER', 'DOCTOR', 'EMPLOYEE', 'ADMIN')")
	public SuccessReponse<ReviewResponseDTO> createReviewForCurrentUser(
			@RequestBody @Valid final ReviewSelfRequestDTO dto) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return SuccessReponse.of(this.reviewSelfService.createReviewForCurrentUser(dto, username));
	}

	// PUT /api/reviews/me/{id} - User cập nhật review của chính mình
	@PutMapping("/me/{id}")
	@PreAuthorize("hasAnyAuthority('USER', 'DOCTOR', 'EMPLOYEE', 'ADMIN')")
	public SuccessReponse<ReviewResponseDTO> updateReviewByCurrentUser(@PathVariable Integer id,
			@RequestBody @Valid ReviewUpdateRequestDTO_USER dto) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return SuccessReponse.of(this.reviewSelfService.updateByCurrentUser(id, dto, username));
	}

	// PUT /api/reviews - Cập nhật
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
	public SuccessReponse<ReviewResponseDTO> updateReview(@RequestBody @Valid ReviewUpdateRequestDTO dto,
			@PathVariable Integer id) {
		return SuccessReponse.of(this.reviewAdminService.update(id, dto));
	}

	// DELETE /api/reviews - Soft delete
	@PutMapping("/status/{id}")
	@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
	public SuccessReponse<ReviewResponseDTO> deleteReview(@RequestBody @Valid ReviewUpdateRequestDTO dto,
			@PathVariable Integer id) {
		return SuccessReponse.of(this.reviewAdminService.delete(id, dto));
	}

	@DeleteMapping("/me/{id}")
	@PreAuthorize("hasAnyAuthority('USER', 'DOCTOR', 'EMPLOYEE', 'ADMIN')")
	public SuccessReponse<ReviewResponseDTO> deleteReviewByCurrentUserUsingDelete(@PathVariable Integer id) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return SuccessReponse.of(this.reviewSelfService.softDeleteByCurrentUser(id, username));
	}

}
