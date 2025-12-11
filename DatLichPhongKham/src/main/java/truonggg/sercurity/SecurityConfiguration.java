package truonggg.sercurity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

	private static final String[] WHITE_LIST = { "/auth/**" };

	private final JwtAuthenticationFilter jwtRequestFilter;
	private final UserDetailsService userDetailsService;
	private final CustomAccessDeniedHandler accessDeniedHandler;
	private final CustomAuthenticationEntryPoint authenticationEntryPoint;

	@Lazy
	public SecurityConfiguration(JwtAuthenticationFilter jwtRequestFilter, UserDetailsService userDetailsService,
			CustomAccessDeniedHandler accessDeniedHandler, CustomAuthenticationEntryPoint authenticationEntryPoint) {
		this.jwtRequestFilter = jwtRequestFilter;
		this.userDetailsService = userDetailsService;
		this.accessDeniedHandler = accessDeniedHandler;
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authConfiguration) throws Exception {
		return authConfiguration.getAuthenticationManager();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(); // salt
	}

	@Bean
	AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(this.userDetailsService);
		authProvider.setPasswordEncoder(this.passwordEncoder());

		return authProvider;
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.addAllowedOriginPattern("*");
		configuration.addAllowedMethod("*");
		configuration.addAllowedHeader("*");
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable);
		http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

		http.authorizeHttpRequests(auths -> auths
				// Public endpoints - không cần xác thực
				.requestMatchers(WHITE_LIST).permitAll().requestMatchers(HttpMethod.POST, "/api/payments/momo-callback")
				.permitAll() // MoMo callback
				.requestMatchers(HttpMethod.GET, "/api/doctors/me", "/api/doctors/me/**").hasAuthority("DOCTOR")
				.requestMatchers(HttpMethod.GET, "/api/doctors", "/api/doctors/*", "/api/doctors/department",
						"/api/doctors/search")
				.permitAll()
				.requestMatchers(HttpMethod.GET, "/api/departments", "/api/departments/*", "/api/departments/search")
				.permitAll().requestMatchers(HttpMethod.GET, "/api/siteinfos").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/schedules/doctor/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/reviews/*", "/api/reviews/doctor/**").permitAll()

				// ADMIN only - quản lý hệ thống
				.requestMatchers("/api/admin/**").hasAuthority("ADMIN")

				// User management (theo @PreAuthorize trong UserController)
				.requestMatchers(HttpMethod.GET, "/api/users").hasAuthority("ADMIN")
				.requestMatchers(HttpMethod.GET, "/api/users/me").hasAnyAuthority("USER", "EMPLOYEE", "ADMIN", "DOCTOR")
				.requestMatchers(HttpMethod.GET, "/api/users/*").hasAnyAuthority("ADMIN", "EMPLOYEE")
				.requestMatchers(HttpMethod.POST, "/api/users").hasAuthority("ADMIN")
				.requestMatchers(HttpMethod.PATCH, "/api/users/*/status").hasAuthority("ADMIN")
				.requestMatchers(HttpMethod.PATCH, "/api/users/*").hasAuthority("ADMIN")
				.requestMatchers(HttpMethod.POST, "/api/users/assign-role").hasAuthority("ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/api/users/manually/*").hasAuthority("ADMIN")
				.requestMatchers(HttpMethod.PUT, "/api/users/profile").hasAnyAuthority("USER", "EMPLOYEE", "ADMIN")

				// Doctor management
				.requestMatchers(HttpMethod.POST, "/api/doctors").hasAuthority("ADMIN")
				.requestMatchers(HttpMethod.PUT, "/api/doctors/profile").hasAnyAuthority("DOCTOR", "ADMIN")
				.requestMatchers(HttpMethod.PUT, "/api/doctors/*").hasAuthority("ADMIN")
				.requestMatchers(HttpMethod.PATCH, "/api/doctors/*").hasAuthority("ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/api/doctors/*").hasAuthority("ADMIN")

				// Schedule management
				.requestMatchers(HttpMethod.GET, "/api/schedules").hasAnyAuthority("EMPLOYEE", "ADMIN")
				.requestMatchers(HttpMethod.POST, "/api/schedules").hasAnyAuthority("EMPLOYEE", "ADMIN")
				.requestMatchers(HttpMethod.PUT, "/api/schedules/*").hasAnyAuthority("EMPLOYEE", "ADMIN")
				.requestMatchers(HttpMethod.PUT, "/api/schedules/status/*").hasAnyAuthority("EMPLOYEE", "ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/api/schedules/*").hasAnyAuthority("EMPLOYEE", "ADMIN")

				// Department management
				.requestMatchers(HttpMethod.POST, "/api/departments").hasAnyAuthority("EMPLOYEE", "ADMIN")
				.requestMatchers(HttpMethod.PUT, "/api/departments/*").hasAnyAuthority("EMPLOYEE", "ADMIN")
				.requestMatchers(HttpMethod.PUT, "/api/departments/status/*").hasAnyAuthority("EMPLOYEE", "ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/api/departments/manually/*").hasAnyAuthority("EMPLOYEE", "ADMIN")

				// Site info management
				.requestMatchers(HttpMethod.POST, "/api/siteinfos").hasAnyAuthority("EMPLOYEE", "ADMIN")
				.requestMatchers(HttpMethod.PUT, "/api/siteinfos").hasAnyAuthority("EMPLOYEE", "ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/api/siteinfos").hasAnyAuthority("EMPLOYEE", "ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/api/siteinfos/*").hasAnyAuthority("EMPLOYEE", "ADMIN")

				// Appointments
				.requestMatchers(HttpMethod.GET, "/api/appointments").hasAnyAuthority("EMPLOYEE", "ADMIN")
				.requestMatchers(HttpMethod.GET, "/api/appointments/*")
				.hasAnyAuthority("USER", "DOCTOR", "EMPLOYEE", "ADMIN")
				.requestMatchers(HttpMethod.POST, "/api/appointments").hasAnyAuthority("USER", "ADMIN")
				.requestMatchers(HttpMethod.PUT, "/api/appointments/*/assign-doctor")
				.hasAnyAuthority("EMPLOYEE", "ADMIN").requestMatchers(HttpMethod.PUT, "/api/appointments/*/cancel-user")
				.hasAuthority("USER").requestMatchers(HttpMethod.PUT, "/api/appointments/*")
				.hasAnyAuthority("EMPLOYEE", "ADMIN").requestMatchers(HttpMethod.PUT, "/api/appointments/*/status")
				.hasAnyAuthority("EMPLOYEE", "ADMIN").requestMatchers(HttpMethod.DELETE, "/api/appointments/*")
				.hasAnyAuthority("EMPLOYEE", "ADMIN")

				// NOTIFICATIONS - Bảo mật các API thông báo
				// Pattern cụ thể trước (me endpoints cho USER/DOCTOR)
				.requestMatchers(HttpMethod.GET, "/api/notifications/me").hasAnyAuthority("USER", "DOCTOR")
				.requestMatchers(HttpMethod.GET, "/api/notifications/me/unread").hasAnyAuthority("USER", "DOCTOR")
				.requestMatchers(HttpMethod.PUT, "/api/notifications/me/*/read").hasAnyAuthority("USER", "DOCTOR")
				.requestMatchers(HttpMethod.DELETE, "/api/notifications/me/*").hasAnyAuthority("USER", "DOCTOR")
				// Pattern cụ thể trước (user endpoints)
				.requestMatchers(HttpMethod.GET, "/api/notifications/user/*")
				.hasAnyAuthority("USER", "DOCTOR", "EMPLOYEE", "ADMIN")
				.requestMatchers(HttpMethod.GET, "/api/notifications/user/*/unread")
				.hasAnyAuthority("USER", "DOCTOR", "EMPLOYEE", "ADMIN")
				// Admin/Employee endpoints
				.requestMatchers(HttpMethod.GET, "/api/notifications").hasAnyAuthority("ADMIN", "EMPLOYEE")
				.requestMatchers(HttpMethod.GET, "/api/notifications/*").hasAnyAuthority("ADMIN", "EMPLOYEE")
				.requestMatchers(HttpMethod.POST, "/api/notifications").hasAnyAuthority("ADMIN", "EMPLOYEE")
				.requestMatchers(HttpMethod.PUT, "/api/notifications").hasAnyAuthority("ADMIN", "EMPLOYEE")
				.requestMatchers(HttpMethod.PUT, "/api/notifications/*/read").hasAnyAuthority("ADMIN", "EMPLOYEE")
				.requestMatchers(HttpMethod.DELETE, "/api/notifications").hasAnyAuthority("ADMIN", "EMPLOYEE")
				.requestMatchers(HttpMethod.DELETE, "/api/notifications/*").hasAnyAuthority("ADMIN", "EMPLOYEE")

				// REVIEWS - Bảo mật các API đánh giá
				.requestMatchers(HttpMethod.GET, "/api/reviews").hasAnyAuthority("EMPLOYEE", "ADMIN")

				// .requestMatchers(HttpMethod.POST, "/api/reviews").hasAnyAuthority("USER",
				// "ADMIN")
				.requestMatchers(HttpMethod.PUT, "/api/reviews/me/**")
				.hasAnyAuthority("USER", "DOCTOR", "EMPLOYEE", "ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/api/reviews/me/**")
				.hasAnyAuthority("USER", "DOCTOR", "EMPLOYEE", "ADMIN")
				.requestMatchers(HttpMethod.PUT, "/api/reviews/*").hasAnyAuthority("ADMIN", "EMPLOYEE")
				.requestMatchers(HttpMethod.PUT, "/api/reviews/status/*").hasAnyAuthority("ADMIN", "EMPLOYEE")
				.requestMatchers(HttpMethod.DELETE, "/api/reviews/*").hasAnyAuthority("ADMIN", "EMPLOYEE")

				// Roles
				.requestMatchers("/api/roles/**").hasAuthority("ADMIN")

				// Tất cả request khác cần xác thực
				.anyRequest().authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authenticationProvider(this.authenticationProvider())
				.addFilterBefore(this.jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
				.exceptionHandling(handler -> handler.accessDeniedHandler(this.accessDeniedHandler)
						.authenticationEntryPoint(this.authenticationEntryPoint));
		return http.build();
	}
}