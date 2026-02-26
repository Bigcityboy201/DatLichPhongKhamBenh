package truonggg.review.application.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import truonggg.Exception.NotFoundException;
import truonggg.user.domain.model.User;
import truonggg.review.domain.model.review;
import truonggg.dto.reponseDTO.ReviewResponseDTO;
import truonggg.dto.requestDTO.ReviewRequestDTO;
import truonggg.dto.requestDTO.ReviewSelfRequestDTO;
import truonggg.dto.requestDTO.ReviewUpdateRequestDTO;
import truonggg.dto.requestDTO.ReviewUpdateRequestDTO_USER;
import truonggg.review.mapper.ReviewMapper;
import truonggg.doctor.infrastructure.DoctorsRepository;
import truonggg.review.infrastructure.ReviewRepository;
import truonggg.user.infrastructure.UserRepository;
import truonggg.reponse.PagedResult;
import truonggg.review.application.ReviewAdminService;
import truonggg.review.application.ReviewSelfService;
import truonggg.doctor.domain.model.Doctors;

@Service
@RequiredArgsConstructor
public class ReviewServiceIMPL implements ReviewAdminService, ReviewSelfService {

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
		// Lấy doctor theo ID
		Doctors doctor = doctorsRepository.findById(doctorId)
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));

		User user = doctor.getUser();

		boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole().getRoleName())
				|| "EMPLOYEE".equalsIgnoreCase(user.getRole().getRoleName());

		Page<review> reviewsPage;

		if (isAdmin) {
			// Admin thấy tất cả
			reviewsPage = reviewRepository.findByDoctorsId(doctorId, pageable);
		} else {
			// Doctor chỉ thấy active
			reviewsPage = reviewRepository.findByDoctorsIdAndIsActive(doctorId, false, pageable);
		}

		return toPagedResult(reviewsPage);
	}

	@Override
	public PagedResult<ReviewResponseDTO> getByCurrentUser(String userName, Pageable pageable) {
		User user = getUserByUserName(userName);

		boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole().getRoleName())
				|| "EMPLOYEE".equalsIgnoreCase(user.getRole().getRoleName());

		Page<review> reviewsPage;

		if (isAdmin) {
			// Admin thấy tất cả review, kể cả bị khóa
			reviewsPage = reviewRepository.findByUser_UserId(user.getUserId(), pageable);
		} else {
			// User bình thường chỉ thấy review đang hoạt động (isActive = true)
			reviewsPage = reviewRepository.findByUser_UserIdAndIsActiveTrue(user.getUserId(), pageable);
		}

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

		return PagedResult.from(reviewsPage, dtoList);
	}

	private User getUserByUserName(String userName) {
		return this.userRepository.findByUserName(userName)
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));
	}
}
