package truonggg.doctor.domain.model;

import jakarta.persistence.*;
import lombok.*;
import truonggg.appointment.domain.model.Appointments;
import truonggg.department.domain.model.Departments;
import truonggg.review.domain.model.review;
import truonggg.schedules.domain.model.Schedules;
import truonggg.user.domain.model.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctors {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int experienceYears;
    private String description;
    private String imageUrl;
    @Column(columnDefinition = "BIT DEFAULT 0")
    private boolean isActive;
    @Column(columnDefinition = "BIT DEFAULT 0")
    private Boolean isFeatured;
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false, unique = true)
    private User user;
    @OneToMany(mappedBy = "doctors", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointments> list = new ArrayList();
    @OneToMany(mappedBy = "doctors")
    private List<review> list1 = new ArrayList<>();
    @OneToMany(mappedBy = "doctors", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Schedules> list2 = new ArrayList<>();
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

    public void activate() {
        if (this.user == null) {
            throw new IllegalStateException("Doctor must be linked to a user");
        }

        if (!this.user.getIsActive()) {
            throw new IllegalStateException("Cannot activate doctor if user is inactive");
        }

        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void ensureValidState() {
        if (this.user == null) {
            throw new IllegalStateException("Doctor must belong to a user");
        }

        if (this.experienceYears < 0) {
            throw new IllegalStateException("Experience years cannot be negative");
        }
    }
}
