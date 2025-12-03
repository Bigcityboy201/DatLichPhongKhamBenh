package truonggg.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import truonggg.Model.Payments;

@Repository
public interface PaymentRepository extends JpaRepository<Payments, Integer> {
	
	// Tìm payment theo transactionId
	Optional<Payments> findByTransactionId(String transactionId);
	
	// Tìm payments theo appointment
	List<Payments> findByAppointments_Id(Integer appointmentId);
	
	// Tìm payments theo user (qua appointment)
	@Query("SELECT p FROM Payments p WHERE p.appointments.user.userId = :userId")
	Page<Payments> findByUserId(@Param("userId") Integer userId, Pageable pageable);
	
	// Tìm payments theo status
	Page<Payments> findByStatus(truonggg.Enum.Appointments_Enum status, Pageable pageable);
	
	// Tìm payments theo payment method
	Page<Payments> findByPaymentMethod(truonggg.Enum.PaymentMethod paymentMethod, Pageable pageable);
}

