package truonggg.appointment.domain.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import truonggg.appointment.domain.model.Appointments;
import truonggg.user.domain.model.User;

@Component
public class AppointmentAccessValidator {

    public void validatePermission(User user, Appointments appointment) {

        boolean isAdminOrEmployee = user.getRole() != null && Boolean.TRUE.equals(user.getRole().getIsActive())
                && ("ADMIN".equals(user.getRole().getRoleName()) || "EMPLOYEE".equals(user.getRole().getRoleName()));

        if (!isAdminOrEmployee && !appointment.getUser().getUserId().equals(user.getUserId())) {

            throw new AccessDeniedException("Bạn không có quyền thanh toán cho appointment này");
        }
    }
}
