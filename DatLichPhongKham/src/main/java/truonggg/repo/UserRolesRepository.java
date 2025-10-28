package truonggg.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import truonggg.Model.UserRoles;

@Repository
public interface UserRolesRepository extends JpaRepository<UserRoles, Integer> {
	
	// Tìm UserRoles theo User ID
	List<UserRoles> findByUserUserId(Integer userId);
	
	// Tìm UserRoles theo Role ID
	List<UserRoles> findByRoleRoleId(Integer roleId);
	
	// Tìm UserRoles theo User ID và Role ID
	Optional<UserRoles> findByUserUserIdAndRoleRoleId(Integer userId, Integer roleId);
	
	// Tìm UserRoles đang active theo User ID
	@Query("SELECT ur FROM UserRoles ur WHERE ur.user.userId = :userId AND ur.isActive = true")
	List<UserRoles> findActiveByUserId(@Param("userId") Integer userId);
	
	// Tìm UserRoles đang active theo Role ID
	@Query("SELECT ur FROM UserRoles ur WHERE ur.role.roleId = :roleId AND ur.isActive = true")
	List<UserRoles> findActiveByRoleId(@Param("roleId") Integer roleId);
	
	// Kiểm tra UserRoles có tồn tại không
	boolean existsByUserUserIdAndRoleRoleId(Integer userId, Integer roleId);
}