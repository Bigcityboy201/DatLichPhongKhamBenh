package truonggg.doctor.domain.service;

import org.springframework.stereotype.Component;
import truonggg.constant.SecurityRole;
import truonggg.doctor.domain.model.Doctors;
import truonggg.role.application.RoleAssignmentHandler;
import truonggg.user.domain.model.User;
@Component
public class DoctorRoleAssignmentHandler implements RoleAssignmentHandler {

    @Override
    public String supportedRole() {
        return SecurityRole.ROLE_DOCTOR;
    }

    @Override
    public void onAssigned(User user) {

        if (user.getDoctors() != null) {
            return;
        }

        Doctors doctor = Doctors.createDefault();
        user.assignDoctorProfile(doctor);
    }

    @Override
    public void onRemoved(User user) {
        user.removeDoctorProfile();
    }
}