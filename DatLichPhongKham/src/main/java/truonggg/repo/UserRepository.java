package truonggg.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import truonggg.Model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
	// Kiểm tra username đã tồn tại chưa
	boolean existsByUserName(String userName);

	// Kiểm tra email đã tồn tại chưa
	boolean existsByEmail(String email);

	// Tìm user theo username và load role (JOIN FETCH để đảm bảo role được load)
	@Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.userName = :username")
	Optional<User> findByUserName(@Param("username") String username);
}
