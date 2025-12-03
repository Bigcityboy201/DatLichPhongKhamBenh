package truonggg.service.IMPL;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import truonggg.Exception.NotFoundException;
import truonggg.Model.Doctors;
import truonggg.Model.User;
import truonggg.Model.review;
import truonggg.dto.reponseDTO.ReviewResponseDTO;
import truonggg.dto.requestDTO.ReviewRequestDTO;
import truonggg.dto.requestDTO.ReviewSelfRequestDTO;
import truonggg.dto.requestDTO.ReviewUpdateRequestDTO;
import truonggg.dto.requestDTO.ReviewUpdateRequestDTO_USER;
import truonggg.mapper.ReviewMapper;
import truonggg.repo.DoctorsRepository;
import truonggg.repo.ReviewRepository;
import truonggg.repo.UserRepository;
import truonggg.reponse.PagedResult;
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
		review = this.reviewRepository.save(review);
		return toResponse(review);
	}

	@Override
	public ReviewResponseDTO createReviewForCurrentUser(ReviewSelfRequestDTO dto, String userName) {
		User user = getUserByUserName(userName);
		Doctors doctors = this.doctorsRepository.findById(dto.getDoctorId())
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));

		review review = this.reviewMapper.toEntity(dto);
		review.setUser(user);
		review.setDoctors(doctors);
		review.setCreateAt(new Date());

		return toResponse(this.reviewRepository.save(review));
	}

	@Override
	public PagedResult<ReviewResponseDTO> getAll(Pageable pageable) {
		Page<review> reviewsPage = this.reviewRepository.findAll(pageable);
		return toPagedResult(reviewsPage);
	}

	@Override
	public PagedResult<ReviewResponseDTO> getByDoctorId(Integer doctorId, Pageable pageable) {
		Page<review> reviewsPage = reviewRepository.findByDoctorsId(doctorId, pageable);
		return toPagedResult(reviewsPage);
	}

	@Override
	public PagedResult<ReviewResponseDTO> getByCurrentUser(String userName, Pageable pageable) {
		User user = getUserByUserName(userName);
		Page<review> reviewsPage = reviewRepository.findByUser_UserId(user.getUserId(), pageable);
		return toPagedResult(reviewsPage);
	}

	@Override
	public ReviewResponseDTO findById(Integer id) {
		review review = this.reviewRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("review", "Review Not Found"));
		return toResponse(review);
	}

	@Override
	public ReviewResponseDTO update(Integer id, ReviewUpdateRequestDTO dto) {
		review foundReview = this.reviewRepository.findById(id)
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

		return toResponse(this.reviewRepository.save(foundReview));
	}

	@Override
	public ReviewResponseDTO updateByCurrentUser(Integer id, ReviewUpdateRequestDTO_USER dto, String userName) {
		review foundReview = this.reviewRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("review", "Review Not Found"));

		// Chỉ cho phép user sở hữu review được sửa
		if (!foundReview.getUser().getUserName().equals(userName)) {
			throw new AccessDeniedException("You cannot modify this review");
		}

		if (dto.getRating() != null) {
			foundReview.setRating(dto.getRating());
		}
		if (dto.getComment() != null) {
			foundReview.setComment(dto.getComment());
		}

		// Không cho phép đổi user / doctor trong API tự sửa review của mình

		return toResponse(this.reviewRepository.save(foundReview));
	}

	@Override
	public ReviewResponseDTO delete(Integer id, ReviewUpdateRequestDTO dto) {
		review foundReview = this.reviewRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("review", "Review Not Found"));

		if (dto.getActive() != null) {
			foundReview.setIsActive(dto.getActive());
		}

		return toResponse(this.reviewRepository.save(foundReview));
	}

	@Override
	public boolean delete(Integer id) {
		review foundReview = this.reviewRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("review", "Review Not Found"));

		this.reviewRepository.delete(foundReview);
		return true;
	}

	@Override
	public ReviewResponseDTO softDeleteByCurrentUser(Integer id, String userName) {
		review foundReview = this.reviewRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("review", "Review Not Found"));

		if (!foundReview.getUser().getUserName().equals(userName)) {
			throw new AccessDeniedException("You cannot modify this review");
		}

		foundReview.setIsActive(true);
		return toResponse(this.reviewRepository.save(foundReview));
	}

	private ReviewResponseDTO toResponse(review review) {
		ReviewResponseDTO dto = this.reviewMapper.toDTO(review);
		dto.setActive(review.getIsActive());
		return dto;
	}

	private PagedResult<ReviewResponseDTO> toPagedResult(Page<review> reviewsPage) {
		List<ReviewResponseDTO> dtoList = reviewsPage.stream().map(this::toResponse).collect(Collectors.toList());

		return PagedResult.<ReviewResponseDTO>builder().content(dtoList)
				.totalElements((int) reviewsPage.getTotalElements()).totalPages(reviewsPage.getTotalPages())
				.currentPage(reviewsPage.getNumber()).pageSize(reviewsPage.getSize()).build();
	}

	private User getUserByUserName(String userName) {
		return this.userRepository.findByUserName(userName)
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));
	}
}
