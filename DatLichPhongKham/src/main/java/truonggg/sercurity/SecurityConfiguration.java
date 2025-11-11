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

	private static final String[] WHITE_LIST = { "/auth/**",
			// Public endpoints cho frontend
			"/api/doctors", "/api/doctors/*", "/api/doctors/department", "/api/departments", "/api/appointments",
			"/api/siteinfos", "/api/reviews/*", "/api/schedules" };

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

//	@Bean
//	PasswordEncoder passwordEncoder2() {
//		return new BCryptPasswordEncoder(); // salt
//	}

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
				.requestMatchers(WHITE_LIST).permitAll()

				// ADMIN only - quản lý hệ thống
				.requestMatchers("/api/admin/**").hasAnyAuthority("ADMIN")

				// EMPLOYEE + ADMIN - quản lý khoa và thông tin site
				.requestMatchers(HttpMethod.POST, "/api/departments").hasAnyAuthority("EMPLOYEE", "ADMIN")
				.requestMatchers(HttpMethod.PUT, "/api/departments").hasAnyAuthority("EMPLOYEE", "ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/api/departments/**").hasAnyAuthority("EMPLOYEE", "ADMIN")
				.requestMatchers(HttpMethod.POST, "/api/siteinfos").hasAnyAuthority("EMPLOYEE", "ADMIN")
				.requestMatchers(HttpMethod.PUT, "/api/siteinfos").hasAnyAuthority("EMPLOYEE", "ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/api/siteinfos/**").hasAnyAuthority("EMPLOYEE", "ADMIN")

				// DOCTOR + ADMIN - quản lý bác sĩ và lịch hẹn
				.requestMatchers(HttpMethod.POST, "/api/doctors").hasAnyAuthority("DOCTOR", "ADMIN")
				.requestMatchers(HttpMethod.PUT, "/api/doctors/**").hasAnyAuthority("DOCTOR", "ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/api/doctors/**").hasAnyAuthority("DOCTOR", "ADMIN")
				.requestMatchers(HttpMethod.GET, "/api/appointments").hasAnyAuthority("DOCTOR", "ADMIN")
				.requestMatchers(HttpMethod.PUT, "/api/appointments").hasAnyAuthority("DOCTOR", "ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/api/appointments/**").hasAnyAuthority("DOCTOR", "ADMIN")
				.requestMatchers("/api/schedules/**").hasAnyAuthority("DOCTOR", "ADMIN")

				// USER + DOCTOR + EMPLOYEE + ADMIN - xem thông tin cơ bản
				.requestMatchers(HttpMethod.GET, "/api/appointments/{id}")
				.hasAnyAuthority("USER", "DOCTOR", "EMPLOYEE", "ADMIN")

				// Tất cả request khác cần xác thực
				.anyRequest().authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authenticationProvider(this.authenticationProvider())
				.addFilterBefore(this.jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
				.exceptionHandling(handler -> handler.accessDeniedHandler(this.accessDeniedHandler))
				.exceptionHandling(handler -> handler.authenticationEntryPoint(this.authenticationEntryPoint));
		return http.build();
	}
}