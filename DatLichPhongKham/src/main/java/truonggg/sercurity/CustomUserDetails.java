package truonggg.sercurity;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import truonggg.Model.Role;
import truonggg.Model.User;

@AllArgsConstructor
@Getter
@Setter
//(1)
public class CustomUserDetails implements UserDetails {
	private static final long serialVersionUID = 1L;
	private final String userName;
	private final String password;
	private final Set<GrantedAuthority> authorities;// role admin
	private final Set<Role> roles;// admin

	public CustomUserDetails(final User user) {
		this.userName = user.getUserName();
		this.password = user.getPassword();
		
		// Lấy role từ User.role thay vì User.list
		if (user.getRole() != null) {
			this.authorities = Set.of(new SimpleGrantedAuthority(user.getRole().getRoleName()));
			this.roles = Set.of(user.getRole());
		} else {
			this.authorities = Set.of();
			this.roles = Set.of();
		}
		
		// Debug: log authorities để kiểm tra
		System.out.println("User: " + this.userName + " has authorities: " + 
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