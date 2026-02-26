package truonggg.Configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import truonggg.role.domain.model.Role;
import truonggg.role.infrastructure.RoleRepository;
import truonggg.user.domain.model.User;
import truonggg.constant.SecurityRole;
import truonggg.user.infrastructure.UserRepository;

@Component
@Profile("dev") // chạy môi trường dev,prod không tự động tạo
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) throws Exception {
		// 1. Tạo role cơ bản nếu chưa có và set isActive=false
		createBasicRolesIfNotExists();

		// 2. Tạo admin user lần đầu, inactive
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
			role = Role.builder().roleName(roleName).Description(description).isActive(true) // role active mặc định
					.build();
			roleRepository.save(role);
			System.out.println("Created role: " + roleName + " with isActive=true");
		} else if (!role.getIsActive()) {
			// Nếu role đã tồn tại nhưng inactive, set lại active
			role.setIsActive(true);
			roleRepository.save(role);
			System.out.println("Updated role: " + roleName + " to isActive=true");
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

			// Tạo user admin active và gán role trực tiếp
			admin = User.create(
					adminUsername,
					passwordEncoder.encode("quangtruong1"),
					"Quang Truong",
					"quangtruong2012004@gmail.com",
                    adminRole
			);
			admin.assignRole(adminRole);
			admin = userRepository.save(admin);
			System.out.println(
					"Created admin user: " + adminUsername + " isActive=true with role_id=" + adminRole.getRoleId());
		} else {
			// Kiểm tra và cập nhật role nếu chưa có
			if (admin.getRole() == null) {
				Role adminRole = roleRepository.findByRoleName(SecurityRole.ROLE_ADMIN);
				if (adminRole != null) {
					admin.assignRole(adminRole);
					userRepository.save(admin);
					System.out.println("Updated admin user: set role_id=" + adminRole.getRoleId());
				}
			}
			System.out.println("Admin user already exists, nothing changed");
		}
	}
}
