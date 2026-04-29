package truonggg.sercurity;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import truonggg.utils.JwtUtils;

/**
 * Interceptor cho kênh inbound của WebSocket/STOMP.
 * - Đọc JWT từ header "Authorization" trong frame CONNECT
 * - Validate và set Authentication vào SecurityContext + Stomp session
 */
@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

	private final JwtUtils jwtUtils;
	private final UserDetailsService userDetailsService;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null) {
			return message;
		}

		StompCommand command = accessor.getCommand();

		if (StompCommand.CONNECT.equals(command)) {
			String authHeader = accessor.getFirstNativeHeader("Authorization");

			if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
				String token = authHeader.substring(7);

				if (!jwtUtils.isTokenExpired(token)) {
					String userName = jwtUtils.extractUsername(token);
					UserDetails userDetails = userDetailsService.loadUserByUsername(userName);

					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());

					// Gắn user vào STOMP session
					accessor.setUser(authentication);

					// Đồng thời set vào SecurityContext cho thread hiện tại
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			}
		} else {
			// Với các frame SEND, SUBSCRIBE,... dùng lại user đã gắn trên session
			java.security.Principal user = accessor.getUser();
			if (user instanceof Authentication) {
				SecurityContextHolder.getContext().setAuthentication((Authentication) user);
			}
		}

		return message;
	}
}


