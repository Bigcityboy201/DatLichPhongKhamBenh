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

    // ===== Domain behaviour =====

    public static Role create(String roleName, String description) {
        if (roleName == null || roleName.isBlank()) {
            throw new IllegalArgumentException("Role name is required");
        }
        Role role = new Role();
        role.roleName = roleName;
        role.Description = description;
        role.isActive = true;
        return role;
    }

    public void updateInfo(String newName, String newDescription) {
        if (newName != null && !newName.isBlank()) {
            this.roleName = newName;
        }
        if (newDescription != null) {
            this.Description = newDescription;
        }
    }

    public void activate() {
        if (this.isActive) {
            throw new IllegalStateException("Role already active");
        }
        this.isActive = true;
    }

    public void deactivate() {
        if (!this.isActive) {
            throw new IllegalStateException("Role already inactive");
        }
        this.isActive = false;
    }

    // Thêm method thủ công cho boolean isActive (giữ lại cho JPA/Lombok tương thích)
    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}