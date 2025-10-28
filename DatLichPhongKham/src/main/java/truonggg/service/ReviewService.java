package truonggg.service;

import java.util.List;

import truonggg.dto.reponseDTO.ReviewResponseDTO;
import truonggg.dto.requestDTO.ReviewDeleteRequestDTO;
import truonggg.dto.requestDTO.ReviewRequestDTO;
import truonggg.dto.requestDTO.ReviewUpdateRequestDTO;

public interface ReviewService {
	ReviewResponseDTO createReview(ReviewRequestDTO dto);

	List<ReviewResponseDTO> getAll();

	List<ReviewResponseDTO> getByDoctorId(Integer doctorId);

	ReviewResponseDTO findById(Integer id);

	ReviewResponseDTO update(ReviewUpdateRequestDTO dto);

	boolean delete(ReviewDeleteRequestDTO dto);

	boolean delete(Integer id);
}
