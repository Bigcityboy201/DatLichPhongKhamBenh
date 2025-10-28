package truonggg.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import truonggg.Model.Appointments;

@Repository
public interface AppointmentsRepository extends JpaRepository<Appointments, Integer> {

}
