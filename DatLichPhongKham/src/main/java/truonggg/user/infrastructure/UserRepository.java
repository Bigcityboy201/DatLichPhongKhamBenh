package truonggg.user.infrastructure;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import truonggg.user.domain.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
	// Kiểm tra username đã tồn tại chưa
	boolean existsByUserName(String userName);

	// Kiểm tra email đã tồn tại chưa
	boolean existsByEmail(String email);

	// Tìm user theo username và load role (JOIN FETCH để đảm bảo role được load)
	@Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.userName = :username")
	Optional<User> findByUserName(@Param("username") String username);

	boolean existsByEmailAndUserIdNot(String email, Integer id);

	// Query với JOIN FETCH role để tránh LazyInitializationException khi mapper map role.roleName
	// COUNT query riêng để hỗ trợ pagination
	@Query(value = "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.role WHERE u.role.roleId = :roleId",
			countQuery = "SELECT COUNT(DISTINCT u) FROM User u WHERE u.role.roleId = :roleId")
	Page<User> findByRole_RoleId(@Param("roleId") Integer roleId, Pageable pageable);
}


