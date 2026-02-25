package truonggg.user.application.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import truonggg.Exception.NotFoundException;
import truonggg.doctor.domain.Doctors;
import truonggg.doctor.infrastructure.DoctorsRepository;
import truonggg.user.domain.event.UserRoleAssignedEvent;
import truonggg.user.domain.model.User;
import truonggg.user.infrastructure.UserRepository;

@Component
@RequiredArgsConstructor
public class DoctorProfileCreationListener {

    private final UserRepository userRepository;
    private final DoctorsRepository doctorsRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(UserRoleAssignedEvent event) {

        if (!event.isDoctor()) {
            return;
        }

        User user = userRepository.findById(event.getUserId())
                .orElseThrow(() -> new NotFoundException("user", "User Not Found"));

        doctorsRepository.findByUser(user).orElseGet(() -> {
            Doctors doctor = new Doctors();
            doctor.setUser(user);
            doctor.setDescription(null);
            doctor.setExperienceYears(0);
            doctor.setIsActive(false);
            doctor.setDepartments(null);
            doctor.setIsFeatured(false);
            doctor.setImageUrl(null);
            return doctorsRepository.save(doctor);
        });
    }
}