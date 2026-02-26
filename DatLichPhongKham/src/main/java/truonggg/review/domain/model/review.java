package truonggg.review.domain.model;

import jakarta.persistence.*;
import lombok.*;
import truonggg.doctor.domain.model.Doctors;
import truonggg.user.domain.model.User;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int rating;
    private String comment;
    private Date createAt;
    @Column(columnDefinition = "BIT DEFAULT 1")
    private boolean isActive;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private User user;
    @ManyToOne
    @JoinColumn(name = "doctors_id", referencedColumnName = "id")
    private Doctors doctors;

    // Thêm method thủ công cho boolean isActive
    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}