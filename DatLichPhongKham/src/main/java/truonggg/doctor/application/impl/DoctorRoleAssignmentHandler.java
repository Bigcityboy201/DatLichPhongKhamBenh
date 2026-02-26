package truonggg.doctor.application.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import truonggg.constant.SecurityRole;
import truonggg.doctor.domain.model.Doctors;
import truonggg.role.application.RoleAssignmentHandler;
import truonggg.user.domain.model.User;
@Component
@RequiredArgsConstructor
public class DoctorRoleAssignmentHandler implements RoleAssignmentHandler {

    @Override
    public String supportedRole() {
        return SecurityRole.ROLE_DOCTOR;
    }

    @Override
    public void onAssigned(User user) {

        if (user.getDoctors() != null) {
            return; // đã có rồi thì thôi
        }

        Doctors doctor = Doctors.builder()
                .user(user)
                .experienceYears(0)
                .isActive(false)
                .isFeatured(false)
                .build();

        user.setDoctors(doctor);
    }

    @Override
    public void onRemoved(User user) {

        user.setDoctors(null); // Hibernate tự xóa
    }
}