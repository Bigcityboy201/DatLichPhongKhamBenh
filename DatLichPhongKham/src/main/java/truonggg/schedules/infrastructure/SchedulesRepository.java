package truonggg.schedules.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import truonggg.schedules.domain.model.Schedules;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SchedulesRepository extends JpaRepository<Schedules, Integer> {

    List<Schedules> findByDoctorsId(Integer doctorId);

    Page<Schedules> findByDoctorsId(Integer doctorId, Pageable pageable);

    boolean existsByDoctors_IdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(Integer doctorId, LocalDateTime startAt,
                                                                              LocalDateTime endAt);
}
