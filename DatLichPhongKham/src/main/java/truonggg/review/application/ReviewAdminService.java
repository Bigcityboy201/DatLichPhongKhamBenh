package truonggg.review.application;

import org.springframework.data.domain.Pageable;

import truonggg.dto.reponseDTO.ReviewResponseDTO;
import truonggg.dto.requestDTO.ReviewRequestDTO;
import truonggg.dto.requestDTO.ReviewUpdateRequestDTO;
import truonggg.reponse.PagedResult;

public interface ReviewAdminService {

	ReviewResponseDTO createReview(ReviewRequestDTO dto);

	PagedResult<ReviewResponseDTO> getAll(Pageable pageable);

	PagedResult<ReviewResponseDTO> getByDoctorId(Integer doctorId, Pageable pageable);

	ReviewResponseDTO findById(Integer id);

	ReviewResponseDTO update(Integer id, ReviewUpdateRequestDTO dto);

	ReviewResponseDTO delete(Integer id, ReviewUpdateRequestDTO dto);

}


