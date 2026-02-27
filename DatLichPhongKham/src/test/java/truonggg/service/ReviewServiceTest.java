package truonggg.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import truonggg.Exception.NotFoundException;
import truonggg.user.domain.model.User;
import truonggg.review.domain.model.review;
import truonggg.dto.reponseDTO.ReviewResponseDTO;
import truonggg.dto.requestDTO.ReviewRequestDTO;
import truonggg.dto.requestDTO.ReviewSelfRequestDTO;
import truonggg.dto.requestDTO.ReviewUpdateRequestDTO_USER;
import truonggg.review.mapper.ReviewMapper;
import truonggg.doctor.infrastructure.DoctorsRepository;
import truonggg.review.infrastructure.ReviewRepository;
import truonggg.user.infrastructure.UserRepository;
import truonggg.reponse.PagedResult;
import truonggg.review.application.impl.ReviewServiceIMPL;
import truonggg.doctor.domain.model.Doctors;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private ReviewMapper reviewMapper;

	@Mock
	private UserRepository userRepository;

	@Mock
	private DoctorsRepository doctorsRepository;

	@InjectMocks
	private ReviewServiceIMPL reviewService;

	@DisplayName("createReview: success when user and doctor exist")
	@Test
	void createReview_ShouldCreate_WhenUserAndDoctorExist() {
		ReviewRequestDTO dto = new ReviewRequestDTO();
		dto.setUserId(1);
		dto.setDoctorId(2);

		User user = org.mockito.Mockito.mock(User.class);
		Doctors doctor = org.mockito.Mockito.mock(Doctors.class);

		review entity = new review();
		review saved = new review();

		when(userRepository.findById(1)).thenReturn(Optional.of(user));
		when(doctorsRepository.findById(2)).thenReturn(Optional.of(doctor));
		when(reviewMapper.toEntity(dto)).thenReturn(entity);
		when(reviewRepository.save(entity)).thenReturn(saved);
		when(reviewMapper.toDTO(saved)).thenReturn(new ReviewResponseDTO());

		ReviewResponseDTO result = reviewService.createReview(dto);

		assertNotNull(result);
		verify(reviewRepository).save(entity);
	}

	@DisplayName("createReview: throw NotFoundException when user not found")
	@Test
	void createReview_ShouldThrow_WhenUserNotFound() {
		ReviewRequestDTO dto = new ReviewRequestDTO();
		dto.setUserId(1);
		dto.setDoctorId(2);

		when(userRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> reviewService.createReview(dto));
		assertEquals("user: User Not Found", ex.getMessage());
	}

	@DisplayName("createReviewForCurrentUser: success when doctor exists")
	@Test
	void createReviewForCurrentUser_ShouldCreate_WhenDoctorExists() {
		ReviewSelfRequestDTO dto = new ReviewSelfRequestDTO();
		dto.setDoctorId(2);
		String username = "user1";

		User user = org.mockito.Mockito.mock(User.class);
		Doctors doctor = org.mockito.Mockito.mock(Doctors.class);

		review entity = new review();

		when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
		when(doctorsRepository.findById(2)).thenReturn(Optional.of(doctor));
		when(reviewMapper.toEntity(dto)).thenReturn(entity);
		when(reviewRepository.save(entity)).thenReturn(entity);
		when(reviewMapper.toDTO(entity)).thenReturn(new ReviewResponseDTO());

		ReviewResponseDTO result = reviewService.createReviewForCurrentUser(dto, username);

		assertNotNull(result);
		verify(reviewRepository).save(entity);
	}

	@DisplayName("getAll: success")
	@Test
	void getAll_ShouldReturnPagedResult() {
		Pageable pageable = PageRequest.of(0, 2);
		review r1 = new review();
		review r2 = new review();

		Page<review> page = new PageImpl<>(List.of(r1, r2), pageable, 2);

		when(reviewRepository.findAll(pageable)).thenReturn(page);
		when(reviewMapper.toDTO(any(review.class))).thenReturn(new ReviewResponseDTO());

		PagedResult<ReviewResponseDTO> result = reviewService.getAll(pageable);

		assertNotNull(result);
		assertEquals(2, result.getContent().size());
		verify(reviewRepository).findAll(pageable);
	}

	@DisplayName("updateByCurrentUser: throw AccessDeniedException when not owner")
	@Test
	void updateByCurrentUser_ShouldThrow_WhenNotOwner() {
		Integer id = 1;
		String username = "user1";

		User otherUser = org.mockito.Mockito.mock(User.class);
		when(otherUser.getUserName()).thenReturn("other");

		review found = new review();
		found.setUser(otherUser);

		when(reviewRepository.findById(id)).thenReturn(Optional.of(found));

		AccessDeniedException ex = assertThrows(AccessDeniedException.class,
				() -> reviewService.updateByCurrentUser(id, new ReviewUpdateRequestDTO_USER(), username));
		assertEquals("You cannot modify this review", ex.getMessage());
	}

	@DisplayName("softDeleteByCurrentUser: success when owner")
	@Test
	void softDeleteByCurrentUser_ShouldSucceed_WhenOwner() {
		Integer id = 1;
		String username = "user1";

		User user = org.mockito.Mockito.mock(User.class);
		when(user.getUserName()).thenReturn(username);

		review found = new review();
		found.setUser(user);

		when(reviewRepository.findById(id)).thenReturn(Optional.of(found));
		when(reviewRepository.save(found)).thenReturn(found);
		when(reviewMapper.toDTO(found)).thenReturn(new ReviewResponseDTO());

		ReviewResponseDTO result = reviewService.softDeleteByCurrentUser(id, username);

		assertNotNull(result);
		assertTrue(found.getIsActive());
		verify(reviewRepository).save(found);
	}
}


