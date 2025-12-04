package truonggg.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import truonggg.Enum.Appointments_Enum;
import truonggg.Enum.PaymentMethod;
import truonggg.Model.Payments;

@Repository
public interface PaymentsRepository extends JpaRepository<Payments, Integer> {
	
	// Tìm payments theo user (qua appointment)
	Page<Payments> findByAppointments_User_UserId(Integer userId, Pageable pageable);
	
	// Tìm payments theo appointment (có phân trang)
	Page<Payments> findByAppointments_Id(Integer appointmentId, Pageable pageable);
	
	// Tìm payments theo appointment (không phân trang)
	List<Payments> findByAppointments_Id(Integer appointmentId);
	
	// Tìm payment theo transactionId
	Optional<Payments> findByTransactionId(String transactionId);
	
	// Tìm tất cả payments có phân trang
	Page<Payments> findAll(Pageable pageable);
	
	// Tìm payments theo status
	Page<Payments> findByStatus(Appointments_Enum status, Pageable pageable);
	
	// Tìm payments theo payment method
	Page<Payments> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);
}


