package truonggg.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import truonggg.Model.review;

@Repository
public interface ReviewRepository extends JpaRepository<review, Integer> {
	List<review> findByDoctorsId(Integer doctorId);
}
