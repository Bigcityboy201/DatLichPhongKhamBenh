package truonggg.user.domain.model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import truonggg.appointment.domain.model.Appointments;
import truonggg.dto.requestDTO.UserUpdateRequestDTO;
import truonggg.notifications.domain.model.Notifications;
import truonggg.role.domain.model.Role;
import truonggg.review.domain.model.review;
import truonggg.doctor.domain.model.Doctors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer userId;
	private String fullName;

	@Column(unique = true)
	private String email;
	private String phone;
	private String userName;
	private String password;
	private String address;
	private Date dateOfBirth;
	private Date createdAt;
	@Column(columnDefinition = "BIT DEFAULT 0")
	private boolean isActive;

	// Thay đổi từ OneToMany sang ManyToOne - mỗi user chỉ có 1 role
	@ManyToOne
	@JoinColumn(name = "role_id", referencedColumnName = "roleId")
	private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Doctors doctors;
	@OneToMany(mappedBy = "user")
	private List<Appointments> list1 = new ArrayList();
	@OneToMany(mappedBy = "user")
	private List<review> list2 = new ArrayList<>();
	@OneToMany(mappedBy = "user")
	private List<Notifications> list3 = new ArrayList<>();

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean getIsActive() {
		return isActive;
	}

    public void activate() {
        if (this.role == null) {
            throw new IllegalStateException("User must have role before activation");
        }
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void changeRole(Role newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        if (this.isActive) {
            throw new IllegalStateException("Cannot change role after user is activated");
        }

        this.role = newRole;
    }

    public void markCreated() {
        if (this.createdAt == null) {
            this.createdAt = new Date(System.currentTimeMillis());
        }
    }

    public void ensureValidState() {
        if (this.email == null || this.email.isBlank()) {
            throw new IllegalStateException("Email cannot be empty");
        }

        if (this.userName == null || this.userName.isBlank()) {
            throw new IllegalStateException("Username cannot be empty");
        }

        if (this.password == null || this.password.isBlank()) {
            throw new IllegalStateException("Password cannot be empty");
        }

        if (this.role == null) {
            throw new IllegalStateException("User must have a role");
        }
    }

    public void assignRole(Role newRole) {

        if (newRole == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        if (this.role != null && this.role.getRoleId().equals(newRole.getRoleId())) {
            throw new IllegalStateException("User already has this role");
        }

        // Rule ví dụ: không cho đổi role khi đang active
        if (this.isActive) {
            throw new IllegalStateException("Cannot change role while user is active");
        }

        this.role = newRole;
    }

    public void updateByAdmin(UserUpdateRequestDTO dto) {

        applyUpdate(dto);
    }

    public void updateBySelf(UserUpdateRequestDTO dto) {

        applyUpdate(dto);
    }

    private void applyUpdate(UserUpdateRequestDTO dto) {

        if (dto.getFullName() != null)
            this.fullName = dto.getFullName();

        if (dto.getEmail() != null)
            this.email = dto.getEmail();

        if (dto.getPhone() != null)
            this.phone = dto.getPhone();

        if (dto.getAddress() != null)
            this.address = dto.getAddress();

        if (dto.getDateOfBirth() != null)
            this.dateOfBirth = dto.getDateOfBirth();

        ensureValidState();


    }
}


