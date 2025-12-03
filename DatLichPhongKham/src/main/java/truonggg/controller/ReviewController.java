package truonggg.controller;

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
import truonggg.service.ReviewService;

@RestController
@RequestMapping(path = "/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	// GET /api/reviews - Lấy tất cả
	@GetMapping
	// @PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
	public SuccessReponse<?> getAllReviews(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<ReviewResponseDTO> pagedResult = this.reviewService.getAll(pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// GET /api/reviews/{id} - Lấy theo ID
	@GetMapping("/{id}")
	public SuccessReponse<ReviewResponseDTO> getReviewById(@PathVariable Integer id) {
		return SuccessReponse.of(this.reviewService.findById(id));
	}

	// GET /api/reviews/doctor/{doctorId} - Lấy theo Doctor ID
	@GetMapping("/doctor/{doctorId}")
	public SuccessReponse<?> getReviewsByDoctorId(@PathVariable Integer doctorId,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		PagedResult<ReviewResponseDTO> pagedResult = this.reviewService.getByDoctorId(doctorId, pageable);
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
		PagedResult<ReviewResponseDTO> pagedResult = this.reviewService.getByCurrentUser(username, pageable);
		return SuccessReponse.ofPaged(pagedResult);
	}

	// POST /api/reviews/me - User tạo review cho chính mình
	@PostMapping("/me")
	@PreAuthorize("hasAnyAuthority('USER', 'DOCTOR', 'EMPLOYEE', 'ADMIN')")
	public SuccessReponse<ReviewResponseDTO> createReviewForCurrentUser(
			@RequestBody @Valid final ReviewSelfRequestDTO dto) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return SuccessReponse.of(this.reviewService.createReviewForCurrentUser(dto, username));
	}

	// PUT /api/reviews/me/{id} - User cập nhật review của chính mình
	@PutMapping("/me/{id}")
	@PreAuthorize("hasAnyAuthority('USER', 'DOCTOR', 'EMPLOYEE', 'ADMIN')")
	public SuccessReponse<ReviewResponseDTO> updateReviewByCurrentUser(@PathVariable Integer id,
			@RequestBody @Valid ReviewUpdateRequestDTO_USER dto) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return SuccessReponse.of(this.reviewService.updateByCurrentUser(id, dto, username));
	}

	// PUT /api/reviews - Cập nhật
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
	public SuccessReponse<ReviewResponseDTO> updateReview(@RequestBody @Valid ReviewUpdateRequestDTO dto,
			@PathVariable Integer id) {
		return SuccessReponse.of(this.reviewService.update(id, dto));
	}

	// DELETE /api/reviews - Soft delete
	@PutMapping("/status/{id}")
	@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
	public SuccessReponse<ReviewResponseDTO> deleteReview(@RequestBody @Valid ReviewUpdateRequestDTO dto,
			@PathVariable Integer id) {
		return SuccessReponse.of(this.reviewService.delete(id, dto));
	}

	// PUT /api/reviews/me/{id}/delete - User tự soft delete review của mình
//	@PutMapping("/me/{id}/delete")
//	@PreAuthorize("hasAnyAuthority('USER', 'DOCTOR', 'EMPLOYEE', 'ADMIN')")
//	public SuccessReponse<ReviewResponseDTO> deleteReviewByCurrentUser(@PathVariable Integer id) {
//		String username = SecurityContextHolder.getContext().getAuthentication().getName();
//		return SuccessReponse.of(this.reviewService.softDeleteByCurrentUser(id, username));
//	}

	// DELETE /api/reviews/me/{id} - User tự soft delete review của mình (HTTP
	// DELETE)
	@DeleteMapping("/me/{id}")
	@PreAuthorize("hasAnyAuthority('USER', 'DOCTOR', 'EMPLOYEE', 'ADMIN')")
	public SuccessReponse<ReviewResponseDTO> deleteReviewByCurrentUserUsingDelete(@PathVariable Integer id) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return SuccessReponse.of(this.reviewService.softDeleteByCurrentUser(id, username));
	}

	// DELETE /api/reviews/{id} - Hard delete
	@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")
	@DeleteMapping("/{id}")
	public SuccessReponse<String> hardDeleteReview(@PathVariable Integer id) {
		this.reviewService.delete(id);
		return SuccessReponse.of("Xóa thành công review!");
	}
}
