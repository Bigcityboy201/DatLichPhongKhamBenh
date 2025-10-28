package truonggg.service.IMPL;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import truonggg.Exception.NotFoundException;
import truonggg.Model.Doctors;
import truonggg.Model.User;
import truonggg.Model.review;
import truonggg.dto.reponseDTO.ReviewResponseDTO;
import truonggg.dto.requestDTO.ReviewDeleteRequestDTO;
import truonggg.dto.requestDTO.ReviewRequestDTO;
import truonggg.dto.requestDTO.ReviewUpdateRequestDTO;
import truonggg.mapper.ReviewMapper;
import truonggg.repo.DoctorsRepository;
import truonggg.repo.ReviewRepository;
import truonggg.repo.UserRepository;
import truonggg.service.ReviewService;

@Service
@RequiredArgsConstructor
public class ReviewServiceIMPL implements ReviewService {

	private final ReviewRepository reviewRepository;
	private final ReviewMapper reviewMapper;
	private final UserRepository userRepository;
	private final DoctorsRepository doctorsRepository;

	@Override
	public ReviewResponseDTO createReview(ReviewRequestDTO dto) {
		User user = this.userRepository.findById(dto.getUserId())
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));
		Doctors doctors = this.doctorsRepository.findById(dto.getDoctorId())
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));

		review review = this.reviewMapper.toEntity(dto);
		review.setUser(user);
		review.setDoctors(doctors);
		review.setCreateAt(new Date());
		review.setIsActive(true);

		review = this.reviewRepository.save(review);
		ReviewResponseDTO response = this.reviewMapper.toDTO(review);
		response.setIsActive(review.getIsActive());
		return response;
	}

	@Override
	public List<ReviewResponseDTO> getAll() {
		List<review> reviews = this.reviewRepository.findAll();
		return reviews.stream().map(review -> {
			ReviewResponseDTO dto = reviewMapper.toDTO(review);
			dto.setIsActive(review.getIsActive());
			return dto;
		}).toList();
	}

	@Override
	public List<ReviewResponseDTO> getByDoctorId(Integer doctorId) {
		List<review> reviews = this.reviewRepository.findByDoctorsId(doctorId);
		return reviews.stream().map(review -> {
			ReviewResponseDTO dto = reviewMapper.toDTO(review);
			dto.setIsActive(review.getIsActive());
			return dto;
		}).toList();
	}

	@Override
	public ReviewResponseDTO findById(Integer id) {
		review review = this.reviewRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("review", "Review Not Found"));
		ReviewResponseDTO dto = this.reviewMapper.toDTO(review);
		dto.setIsActive(review.getIsActive());
		return dto;
	}

	@Override
	public ReviewResponseDTO update(ReviewUpdateRequestDTO dto) {
		review foundReview = this.reviewRepository.findById(dto.getId())
				.orElseThrow(() -> new NotFoundException("review", "Review Not Found"));

		if (dto.getRating() != null) {
			foundReview.setRating(dto.getRating());
		}
		if (dto.getComment() != null) {
			foundReview.setComment(dto.getComment());
		}

		// Nếu có thay đổi user
		if (dto.getUserId() != null) {
			User user = this.userRepository.findById(dto.getUserId())
					.orElseThrow(() -> new NotFoundException("user", "User Not Found"));
			foundReview.setUser(user);
		}

		// Nếu có thay đổi doctor
		if (dto.getDoctorId() != null) {
			Doctors doctors = this.doctorsRepository.findById(dto.getDoctorId())
					.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));
			foundReview.setDoctors(doctors);
		}

		review savedReview = this.reviewRepository.save(foundReview);
		ReviewResponseDTO response = this.reviewMapper.toDTO(savedReview);
		response.setIsActive(savedReview.getIsActive());
		return response;
	}

	@Override
	public boolean delete(ReviewDeleteRequestDTO dto) {
		review foundReview = this.reviewRepository.findById(dto.getId())
				.orElseThrow(() -> new NotFoundException("review", "Review Not Found"));

		if (dto.getIsActive() != null) {
			foundReview.setIsActive(dto.getIsActive());
			this.reviewRepository.save(foundReview);
		}
		return true;
	}

	@Override
	public boolean delete(Integer id) {
		review foundReview = this.reviewRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("review", "Review Not Found"));

		this.reviewRepository.delete(foundReview);
		return true;
	}
}
