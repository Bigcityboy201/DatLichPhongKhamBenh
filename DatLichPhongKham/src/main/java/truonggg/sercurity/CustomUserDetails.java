package truonggg.sercurity;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import truonggg.role.domain.model.Role;
import truonggg.user.domain.model.User;

@AllArgsConstructor
@Getter
@Setter
//(1)
public class CustomUserDetails implements UserDetails {
	private static final Logger logger = LoggerFactory.getLogger(CustomUserDetails.class);
	private static final long serialVersionUID = 1L;
	private final String userName;
	private final String password;
	private final Set<GrantedAuthority> authorities;// role admin
	private final Set<Role> roles;// admin

	public CustomUserDetails(final User user) {
		this.userName = user.getUserName();
		this.password = user.getPassword();
		
		// Lấy role từ User.role
		// Logic mới: isActive = true nghĩa là role đang hoạt động
		if (user.getRole() != null && user.getRole().getIsActive()) {
			this.authorities = Set.of(new SimpleGrantedAuthority(user.getRole().getRoleName()));
			this.roles = Set.of(user.getRole());
		} else {
			this.authorities = Set.of();
			this.roles = Set.of();
			if (user.getRole() == null) {
				logger.warn("User {} has no role assigned!", this.userName);
			} else if (!user.getRole().getIsActive()) {
				logger.warn("User {} has inactive role (ngưng): {}", this.userName, user.getRole().getRoleName());
			}
		}
		
		// Debug: log authorities để kiểm tra
		logger.debug("User: {} has authorities: {}", this.userName, 
			this.authorities.stream().map(auth -> auth.getAuthority()).collect(Collectors.toList()));
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.userName;
	}
}