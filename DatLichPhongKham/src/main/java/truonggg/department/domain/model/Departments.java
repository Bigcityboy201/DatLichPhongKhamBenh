package truonggg.department.domain.model;

import jakarta.persistence.*;
import lombok.*;
import truonggg.doctor.domain.model.Doctors;
import truonggg.domain.model.BaseEntity;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
//@Setter
@NoArgsConstructor
//@AllArgsConstructor
//@Builder
public class Departments extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String description;
    @Column(columnDefinition = "BIT DEFAULT 1")
    private boolean isActive;
    @OneToMany(mappedBy = "departments")
    private List<Doctors> list = new ArrayList<>();

    public boolean getIsActive() {
        return isActive;
    }

    public static Departments create(String name, String description) {

        if (!truonggg.utils.ValidationUtils.isNotBlank(name)) {
            throw new IllegalArgumentException("Department name is required");
        }

        Departments department = new Departments();
        department.name = name;
        department.description = description;
        department.isActive = true; // default business rule

        return department;
    }
    public void changeInfo(String name, String description) {

        if (name != null) {
            if (!truonggg.utils.ValidationUtils.isNotBlank(name)) {
                throw new IllegalArgumentException("Department name cannot be blank");
            }
            this.name = name;
        }

        if (description != null) {
            this.description = description;
        }
    }

    public void activate() {
        if (this.isActive) {
            throw new IllegalStateException("Department already active");
        }
        this.isActive = true;
    }

    public void deactivate() {
        if (!this.isActive) {
            throw new IllegalStateException("Department already inactive");
        }
        this.isActive = false;
    }
}
