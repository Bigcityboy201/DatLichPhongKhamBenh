package truonggg.department.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import truonggg.department.domain.model.Departments;

@Repository
public interface DepartmentsRepository extends JpaRepository<Departments, Integer> {
	Page<Departments> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
