package truonggg.schedules.domain.model;

import jakarta.persistence.*;
import lombok.*;
import truonggg.Enum.DayOfWeek;
import truonggg.doctor.domain.model.Doctors;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private DayOfWeek dayOfWeek;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    @Column(columnDefinition = "BIT DEFAULT 1")
    private boolean isActive;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    private Doctors doctors;

    // Thêm method thủ công cho boolean isActive
    public boolean getIsActive() {
        return isActive;
    }

    private Schedules(
            DayOfWeek dayOfWeek,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Doctors doctor
    ) {
        this.dayOfWeek = dayOfWeek;
        this.startAt = startAt;
        this.endAt = endAt;
        this.doctors = doctor;
    }

    public static Schedules create(
            DayOfWeek dayOfWeek,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Doctors doctor
    ) {
        if (doctor == null)
            throw new IllegalArgumentException("Doctor is required");

        return new Schedules(dayOfWeek, startAt, endAt, doctor);
    }

    public void deactivate() {
        if (!isActive)
            throw new IllegalStateException("Already inactive");

        this.isActive = false;
    }

    public void activate() {
        if (isActive)
            throw new IllegalStateException("Already active");

        this.isActive = true;
    }

    public void changeDay(DayOfWeek newDay) {
        if (newDay == null) {
            throw new IllegalArgumentException("Day of week is required");
        }

        this.dayOfWeek = newDay;
    }

    public void changeTime(LocalDateTime newStart, LocalDateTime newEnd) {

        if (newStart == null || newEnd == null) {
            throw new IllegalArgumentException("Start time and end time are required");
        }

        if (!newEnd.isAfter(newStart)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        this.startAt = newStart;
        this.endAt = newEnd;
    }
}