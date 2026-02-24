package truonggg.review.application;

import org.springframework.data.domain.Pageable;

import truonggg.dto.reponseDTO.ReviewResponseDTO;
import truonggg.dto.requestDTO.ReviewSelfRequestDTO;
import truonggg.dto.requestDTO.ReviewUpdateRequestDTO_USER;
import truonggg.reponse.PagedResult;

public interface ReviewSelfService {

	ReviewResponseDTO createReviewForCurrentUser(ReviewSelfRequestDTO dto, String userName);

	PagedResult<ReviewResponseDTO> getByCurrentUser(String userName, Pageable pageable);

	ReviewResponseDTO updateByCurrentUser(Integer id, ReviewUpdateRequestDTO_USER dto, String userName);

	ReviewResponseDTO softDeleteByCurrentUser(Integer id, String userName);
}


