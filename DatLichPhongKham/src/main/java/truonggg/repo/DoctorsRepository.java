package truonggg.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import truonggg.Model.Doctors;

@Repository
public interface DoctorsRepository extends JpaRepository<Doctors, Integer> {
	// List<Doctors> findByDepartmentId(Integer departmentId, Pageable pageable);
	List<Doctors> findTop5ByIsFeaturedTrueOrderByIdAsc();

	// List<Doctors> findByDepartmentIdAndIsActiveTrue(Integer departmentId,
	// Pageable pageable);

	Page<Doctors> findByDepartmentsId(Integer departmentId, Pageable pageable);

	@Query("SELECT d FROM Doctors d LEFT JOIN FETCH d.list2 WHERE d.id = :id")
	Optional<Doctors> findByIdWithSchedules(@Param("id") Integer id);

}
