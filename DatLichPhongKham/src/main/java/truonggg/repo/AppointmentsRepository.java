package truonggg.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import truonggg.Model.Appointments;

@Repository
public interface AppointmentsRepository extends JpaRepository<Appointments, Integer> {

	// List<Appointments> findByUserId(Integer id);
	Page<Appointments> findByUserUserId(Integer userId, Pageable pageable);
}
