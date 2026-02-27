package truonggg.department.domain.model;

import jakarta.persistence.*;
import lombok.*;
import truonggg.doctor.domain.model.Doctors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
//@Setter
@NoArgsConstructor
//@AllArgsConstructor
//@Builder
public class Departments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String description;
    @Column(columnDefinition = "BIT DEFAULT 1")
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "departments")
    private List<Doctors> list = new ArrayList<>();

    public boolean getIsActive() {
        return isActive;
    }

    public static Departments create(String name, String description) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Department name is required");
        }

        Departments department = new Departments();
        department.name = name;
        department.description = description;
        department.isActive = true; // default business rule
        department.createdAt = LocalDateTime.now();
        department.updatedAt = LocalDateTime.now();

        return department;
    }
    public void changeInfo(String name, String description) {

        if (name != null) {
            if (name.isBlank()) {
                throw new IllegalArgumentException("Department name cannot be blank");
            }
            this.name = name;
        }

        if (description != null) {
            this.description = description;
        }

        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        if (this.isActive) {
            throw new IllegalStateException("Department already active");
        }
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        if (!this.isActive) {
            throw new IllegalStateException("Department already inactive");
        }
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
}
