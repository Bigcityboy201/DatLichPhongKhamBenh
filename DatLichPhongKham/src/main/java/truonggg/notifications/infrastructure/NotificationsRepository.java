package truonggg.notifications.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import truonggg.notifications.domain.model.Notifications;

import java.util.List;

@Repository
public interface NotificationsRepository extends JpaRepository<Notifications, Integer> {

    List<Notifications> findByUserUserId(Integer userId);

    List<Notifications> findByUserUserIdAndIsRead(Integer userId, boolean isRead);

    Page<Notifications> findByUserUserId(Integer userId, Pageable pageable);

    Page<Notifications> findByUserUserIdAndIsRead(Integer userId, boolean isRead, Pageable pageable);
}
