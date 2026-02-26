package truonggg.role.domain.model;

import jakarta.persistence.*;
import lombok.*;
import truonggg.user.domain.model.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoleID")
    private Integer roleId;
    @Column(name = "RoleName")
    private String roleName;
    private String Description;
    @Column(columnDefinition = "BIT DEFAULT 1")
    private boolean isActive;
    // Mỗi user có 1 role, nên không cần list UserRoles nữa
    @OneToMany(mappedBy = "role")
    private List<User> users = new ArrayList();

    // Thêm method thủ công cho boolean isActive
    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}