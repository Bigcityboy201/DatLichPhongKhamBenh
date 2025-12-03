package truonggg.service;

import org.springframework.data.domain.Pageable;

import truonggg.dto.reponseDTO.ReviewResponseDTO;
import truonggg.dto.requestDTO.ReviewRequestDTO;
import truonggg.dto.requestDTO.ReviewSelfRequestDTO;
import truonggg.dto.requestDTO.ReviewUpdateRequestDTO;
import truonggg.dto.requestDTO.ReviewUpdateRequestDTO_USER;
import truonggg.reponse.PagedResult;

public interface ReviewService {
	ReviewResponseDTO createReview(ReviewRequestDTO dto);

	ReviewResponseDTO createReviewForCurrentUser(ReviewSelfRequestDTO dto, String userName);

	PagedResult<ReviewResponseDTO> getAll(Pageable pageable);

	PagedResult<ReviewResponseDTO> getByDoctorId(Integer doctorId, Pageable pageable);

	PagedResult<ReviewResponseDTO> getByCurrentUser(String userName, Pageable pageable);

	ReviewResponseDTO findById(Integer id);

	ReviewResponseDTO update(Integer id, ReviewUpdateRequestDTO dto);

	ReviewResponseDTO delete(Integer id, ReviewUpdateRequestDTO dto);

	boolean delete(Integer id);

	ReviewResponseDTO updateByCurrentUser(Integer id, ReviewUpdateRequestDTO_USER dto, String userName);

	ReviewResponseDTO softDeleteByCurrentUser(Integer id, String userName);
}
