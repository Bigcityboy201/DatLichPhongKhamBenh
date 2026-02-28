package truonggg.user.domain.model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import truonggg.appointment.domain.model.Appointments;
import truonggg.notifications.domain.model.Notifications;
import truonggg.role.domain.model.Role;
import truonggg.review.domain.model.review;
import truonggg.doctor.domain.model.Doctors;

@Entity
@Getter
//@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor
//@Builder
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
	@Column(columnDefinition = "BIT DEFAULT 1")
	private boolean isActive;

	// Thay đổi từ OneToMany sang ManyToOne - mỗi user chỉ có 1 role
	@ManyToOne
	@JoinColumn(name = "role_id", referencedColumnName = "roleId")
	private Role role;

	// @JsonIgnore để tránh serialize các collection lazy, gây lỗi "force initializing collection loading"
	@JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Doctors doctors;
	@JsonIgnore
	@OneToMany(mappedBy = "user")
	private List<Appointments> list1 = new ArrayList();
	@JsonIgnore
	@OneToMany(mappedBy = "user")
	private List<review> list2 = new ArrayList<>();
	@JsonIgnore
	@OneToMany(mappedBy = "user")
	private List<Notifications> list3 = new ArrayList<>();

    public static User create(
            String username,
            String password,
            String fullName,
            String email,
            Role role
    ) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        User user = new User();
        user.userName = username;
        user.password = password;
        user.fullName = fullName;
        user.email = email;
        user.role = role;
        user.isActive = true;
        user.validateInvariant();
        return user;
    }

    public void assignRole(Role newRole) {

        if (newRole == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        this.role = newRole;
        validateInvariant();
    }

    public void assignDoctorProfile(Doctors doctor) {

        if (doctor == null) {
            throw new IllegalArgumentException("Doctor profile cannot be null");
        }

        if (this.doctors != null) {
            throw new IllegalStateException("Doctor profile already exists");
        }

        this.doctors = doctor;
        doctor.attachToUser(this);
    }

    public void removeDoctorProfile() {

        if (this.doctors == null) {
            return;
        }

        this.doctors.detachUser();
        this.doctors = null;
    }

    public void updateProfile(
            String fullName,
            String email,
            String phone,
            String address,
            Date dob
    ) {

        if (fullName != null) this.fullName = fullName;
        if (email != null) this.email = email;
        if (phone != null) this.phone = phone;
        if (address != null) this.address = address;
        if (dob != null) this.dateOfBirth = dob;

        validateInvariant();
    }

    public void activate() {
        this.isActive = true;
        validateInvariant();
    }

    public boolean getIsActive() {
        return isActive;
    }

    private void validateInvariant() {

        if (userName == null || userName.isBlank()) {
            throw new IllegalStateException("Username is required");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Password is required");
        }

        if (role == null) {
            throw new IllegalStateException("Role must be assigned");
        }
    }

    public void deactivate() {
        this.isActive = false;
    }
}


