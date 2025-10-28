package truonggg.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import truonggg.Model.Notifications;

@Repository
public interface NotificationsRepository extends JpaRepository<Notifications, Integer> {
	
	List<Notifications> findByUserUserId(Integer userId);
	
	List<Notifications> findByUserUserIdAndIsRead(Integer userId, boolean isRead);
}
