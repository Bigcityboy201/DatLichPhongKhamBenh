package truonggg.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import truonggg.Model.Appointments;
import truonggg.Model.Payments;

@Repository
public interface PaymentsRepository extends JpaRepository<Payments, Integer> {
	
	Page<Payments> findByAppointments_User_UserId(Integer userId, Pageable pageable);
	
	Page<Payments> findByAppointments_Id(Integer appointmentId, Pageable pageable);
	
	List<Payments> findByAppointments_Id(Integer appointmentId);
	
	Optional<Payments> findByTransactionId(String transactionId);
	
	Page<Payments> findAll(Pageable pageable);
}


