package truonggg.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import truonggg.Model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
	// Kiểm tra username đã tồn tại chưa
	boolean existsByUserName(String userName);

	// Kiểm tra email đã tồn tại chưa
	boolean existsByEmail(String email);

	Optional<User> findByUserName(String username);
}
