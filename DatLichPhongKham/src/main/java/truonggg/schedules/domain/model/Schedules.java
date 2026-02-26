package truonggg.schedules.domain.model;

import jakarta.persistence.*;
import lombok.*;
import truonggg.doctor.domain.model.Doctors;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private DayOfWeek dayOfWeek;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    @Column(columnDefinition = "BIT DEFAULT 0")
    private boolean isActive;
    @ManyToOne
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    private Doctors doctors;

    // Thêm method thủ công cho boolean isActive
    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}