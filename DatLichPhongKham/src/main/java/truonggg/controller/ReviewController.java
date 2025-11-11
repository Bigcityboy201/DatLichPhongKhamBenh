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
import truonggg.dto.reponseDTO.ReviewResponseDTO;
import truonggg.dto.requestDTO.ReviewRequestDTO;
import truonggg.dto.requestDTO.ReviewUpdateRequestDTO;
import truonggg.reponse.SuccessReponse;
import truonggg.service.ReviewService;

@RestController
@RequestMapping(path = "/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	// GET /api/reviews - Lấy tất cả
	@GetMapping
	public SuccessReponse<List<ReviewResponseDTO>> getAllReviews() {
		return SuccessReponse.of(this.reviewService.getAll());
	}

	// GET /api/reviews/{id} - Lấy theo ID
	@GetMapping("/{id}")
	public SuccessReponse<ReviewResponseDTO> getReviewById(@PathVariable Integer id) {
		return SuccessReponse.of(this.reviewService.findById(id));
	}

	// GET /api/reviews/doctor/{doctorId} - Lấy theo Doctor ID
	@GetMapping("/doctor/{doctorId}")
	public SuccessReponse<List<ReviewResponseDTO>> getReviewsByDoctorId(@PathVariable Integer doctorId) {
		return SuccessReponse.of(this.reviewService.getByDoctorId(doctorId));
	}

	// POST /api/reviews - Tạo mới
	@PostMapping
	public SuccessReponse<ReviewResponseDTO> createReview(@RequestBody @Valid final ReviewRequestDTO dto) {
		return SuccessReponse.of(this.reviewService.createReview(dto));
	}

	// PUT /api/reviews - Cập nhật
	@PutMapping("/{id}")
	public SuccessReponse<ReviewResponseDTO> updateReview(@RequestBody @Valid ReviewUpdateRequestDTO dto,
			@PathVariable Integer id) {
		return SuccessReponse.of(this.reviewService.update(id, dto));
	}

	// DELETE /api/reviews - Soft delete
	@PutMapping("/status/{id}")
	public SuccessReponse<ReviewResponseDTO> deleteReview(@RequestBody @Valid ReviewUpdateRequestDTO dto,
			@PathVariable Integer id) {
		return SuccessReponse.of(this.reviewService.delete(id, dto));
	}

	// DELETE /api/reviews/{id} - Hard delete
	@DeleteMapping("/{id}")
	public SuccessReponse<String> hardDeleteReview(@PathVariable Integer id) {
		this.reviewService.delete(id);
		return SuccessReponse.of("Xóa thành công review!");
	}
}
