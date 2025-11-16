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
	// private final UserRolesRepository userRolesRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) throws Exception {
		// 1. Tạo role cơ bản nếu chưa có và set isActive=false
		createBasicRolesIfNotExists();

		// 2. Tạo admin user lần đầu, inactive, lưu vào user_roles
		createAdminUserIfNotExists();
	}

	private void createBasicRolesIfNotExists() {
		createRoleIfNotExists(SecurityRole.ROLE_ADMIN, "Administrator role");
		createRoleIfNotExists(SecurityRole.ROLE_USER, "Regular user role");
		createRoleIfNotExists(SecurityRole.ROLE_EMPLOYEE, "Employee role");
		createRoleIfNotExists(SecurityRole.ROLE_DOCTOR, "Doctor role");
	}

	private void createRoleIfNotExists(String roleName, String description) {
		Role role = roleRepository.findByRoleName(roleName);
		if (role == null) {
			role = Role.builder().roleName(roleName).Description(description).isActive(false) // tất cả role inactive
					.build();
			roleRepository.save(role);
			System.out.println("Created role: " + roleName + " with isActive=false");
		} else if (role.getIsActive()) {
			// Nếu role đã tồn tại nhưng active=true, set lại false
			role.setIsActive(false);
			roleRepository.save(role);
			System.out.println("Updated role: " + roleName + " to isActive=false");
		}
	}

	private void createAdminUserIfNotExists() {
		String adminUsername = "quangtruongngo2012004";

		User admin = userRepository.findByUserName(adminUsername).orElse(null);

		if (admin == null) {
			// Lấy role ADMIN trước khi tạo user
			Role adminRole = roleRepository.findByRoleName(SecurityRole.ROLE_ADMIN);

			if (adminRole == null) {
				System.out.println("ERROR: ADMIN role not found! Please create roles first.");
				return;
			}

			// Tạo user admin inactive và gán role trực tiếp
			admin = User.builder().userName(adminUsername).password(passwordEncoder.encode("quangtruong1"))
					.fullName("Quang Truong").email("quangtruong2012004@gmail.com").phone("0123456789").isActive(false)
					.role(adminRole) // Gán đối tượng Role, không phải null
					.build();
			admin = userRepository.save(admin);
			System.out.println(
					"Created admin user: " + adminUsername + " isActive=false with role_id=" + adminRole.getRoleId());

			// Tạo record vào user_roles
//			UserRoles userRole = UserRoles.builder().user(admin).role(adminRole).isActive(false) // role inactive cho
//																									// user
//					.build();

			// userRolesRepository.save(userRole);
			System.out.println("Assigned ADMIN role to admin in user_roles with isActive=false");
		} else {
			// Kiểm tra và cập nhật role nếu chưa có
			if (admin.getRole() == null) {
				Role adminRole = roleRepository.findByRoleName(SecurityRole.ROLE_ADMIN);
				if (adminRole != null) {
					admin.setRole(adminRole);
					userRepository.save(admin);
					System.out.println("Updated admin user: set role_id=" + adminRole.getRoleId());
				}
			}
			System.out.println("Admin user already exists, nothing changed");
		}
	}
}
