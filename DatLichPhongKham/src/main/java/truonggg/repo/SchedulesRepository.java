package truonggg.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import truonggg.Model.Schedules;

@Repository
public interface SchedulesRepository extends JpaRepository<Schedules, Integer> {
	
	List<Schedules> findByDoctorsId(Integer doctorId);
}
