package truonggg.review.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import truonggg.review.domain.model.review;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<review, Integer> {
    List<review> findByDoctorsId(Integer doctorId);

    Page<review> findByDoctorsId(Integer doctorId, Pageable pageable);

    Page<review> findByUser_UserId(Integer userId, Pageable pageable);

    Page<review> findByUser_UserIdAndIsActiveFalse(Integer userId, Pageable pageable);

    Page<review> findByDoctorsIdAndIsActive(Integer doctorId, boolean isActive, Pageable pageable);

}