package truonggg.department.domain.model;

import jakarta.persistence.*;
import lombok.*;
import truonggg.doctor.domain.model.Doctors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Departments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String description;
    @Column(columnDefinition = "BIT DEFAULT 0")
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "departments")
    private List<Doctors> list = new ArrayList<>();

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
