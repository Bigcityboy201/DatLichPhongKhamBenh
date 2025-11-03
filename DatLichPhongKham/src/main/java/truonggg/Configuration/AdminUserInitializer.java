package truonggg.Configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import truonggg.Model.Role;
import truonggg.Model.User;
import truonggg.constant.SecurityRole;
import truonggg.repo.RoleRepository;
import truonggg.repo.UserRepository;

@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) throws Exception {
		// Tạo roles cơ bản nếu chưa có
		createBasicRolesIfNotExists();
		// Tạo user ADMIN nếu chưa có
		createAdminUserIfNotExists();
	}

	private void createBasicRolesIfNotExists() {
		// Tạo role ADMIN
		if (roleRepository.findByRoleName(SecurityRole.ROLE_ADMIN) == null) {
			Role adminRole = Role.builder().roleName(SecurityRole.ROLE_ADMIN).Description("Administrator role")
					.isActive(true).build();
			roleRepository.save(adminRole);
			System.out.println("Created ADMIN role");
		}

		// Tạo role USER
		if (roleRepository.findByRoleName(SecurityRole.ROLE_USER) == null) {
			Role userRole = Role.builder().roleName(SecurityRole.ROLE_USER).Description("Regular user role")
					.isActive(true).build();
			roleRepository.save(userRole);
			System.out.println("Created USER role");
		}

		// Tạo role EMPLOYEE
		if (roleRepository.findByRoleName(SecurityRole.ROLE_EMPLOYEE) == null) {
			Role employeeRole = Role.builder().roleName(SecurityRole.ROLE_EMPLOYEE).Description("Employee role")
					.isActive(true).build();
			roleRepository.save(employeeRole);
			System.out.println("Created EMPLOYEE role");
		}

		// Tạo role DOCTOR
		if (roleRepository.findByRoleName(SecurityRole.ROLE_DOCTOR) == null) {
			Role doctorRole = Role.builder().roleName(SecurityRole.ROLE_DOCTOR).Description("Doctor role")
					.isActive(true).build();
			roleRepository.save(doctorRole);
			System.out.println("Created DOCTOR role");
		}
	}

	private void createAdminUserIfNotExists() {
		String adminUsername = "quangtruongngo2012004";

		// Tạo user admin nếu chưa có
		User admin = userRepository.findByUserName(adminUsername).orElse(null);
		if (admin == null) {
			// Lấy role ADMIN
			Role adminRole = roleRepository.findByRoleName(SecurityRole.ROLE_ADMIN);

			admin = User.builder().userName(adminUsername).password(passwordEncoder.encode("quangtruong1"))
					.fullName("Quang Truong").email("quangtruong2012004@gmail.com").phone("0123456789").isActive(false)
					.role(adminRole) // Gán role ADMIN trực tiếp
					.build();

			admin = userRepository.save(admin);
			System.out.println("Created admin user: " + adminUsername + " with ADMIN role");
		} else {
			// Nếu user đã tồn tại nhưng chưa có role, gán role ADMIN
			if (admin.getRole() == null) {
				Role adminRole = roleRepository.findByRoleName(SecurityRole.ROLE_ADMIN);
				if (adminRole != null) {
					admin.setRole(adminRole);
					userRepository.save(admin);
					System.out.println("Assigned ADMIN role to existing user: " + adminUsername);
				}
			} else {
				System.out.println("User " + adminUsername + " already has role: " + admin.getRole().getRoleName());
			}
		}
	}
}
