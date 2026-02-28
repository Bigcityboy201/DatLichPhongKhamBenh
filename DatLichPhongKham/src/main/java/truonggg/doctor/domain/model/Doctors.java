package truonggg.doctor.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import lombok.*;
import truonggg.Enum.DayOfWeek;
import truonggg.appointment.domain.model.Appointments;
import truonggg.department.domain.model.Departments;
import truonggg.review.domain.model.review;
import truonggg.schedules.domain.model.Schedules;
import truonggg.user.domain.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
//@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor
//@Builder
public class Doctors {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int experienceYears;
    private String description;
    private String imageUrl;
    @Column(columnDefinition = "BIT DEFAULT 1")
    private boolean isActive;
    @Column(columnDefinition = "BIT DEFAULT 0")
    private Boolean isFeatured;
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false, unique = true)
    private User user;
    @OneToMany(mappedBy = "doctors", fetch = FetchType.LAZY,cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointments> list = new ArrayList();
    @OneToMany(mappedBy = "doctors")
    private List<review> list1 = new ArrayList<>();
    @OneToMany(mappedBy = "doctors", fetch = FetchType.LAZY,orphanRemoval = true,cascade = CascadeType.ALL)
    private List<Schedules> schedules = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "departments_id", referencedColumnName = "id")
    private Departments departments;

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public static Doctors createDefault() {
        Doctors doctor = new Doctors();
        doctor.experienceYears = 0;
        doctor.isActive = true;
        doctor.isFeatured = false;
        return doctor;
    }

    public void attachToUser(User user) {
        this.user = user;
    }

    public void detachUser() {
        this.user = null;
    }

    public void updateProfile(
            Integer experienceYears,
            String description,
            String imageUrl,
            Departments departments
    ) {

        if (experienceYears != null) {
            this.experienceYears = experienceYears;
        }

        if (description != null) {
            this.description = description;
        }

        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }

        if (departments != null) {
            this.departments = departments;
        }
    }

    public void updateByAdmin(
            Integer experienceYears,
            String description,
            String imageUrl,
            Boolean isFeatured,
            Departments departments
    ) {

        if (experienceYears != null) {
            this.experienceYears = experienceYears;
        }

        if (description != null) {
            this.description = description;
        }

        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }

        if (isFeatured != null) {
            this.isFeatured = isFeatured;
        }

        if (departments != null) {
            this.departments = departments;
        }
    }

    public void deactivate() {
        if (!this.isActive) {
            throw new IllegalStateException("Doctor already inactive");
        }
        this.isActive = true;
    }
}
