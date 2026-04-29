package truonggg.domain.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import truonggg.domain.event.UserCreatedEvent;
import truonggg.domain.event.RoleAssignedEvent;

/**
 * Event listener for user-related domain events.
 * Handles side effects like logging, notifications, etc.
 */
@Component
public class UserEventListener {

	private static final Logger logger = LoggerFactory.getLogger(UserEventListener.class);

	/**
	 * Handles UserCreatedEvent asynchronously.
	 * Can be extended to send welcome emails, create default profiles, etc.
	 */
	@Async
	@EventListener
	public void handleUserCreated(UserCreatedEvent event) {
		logger.info("User created event received: userId={}, username={}, source={}", 
			event.getUser().getUserId(), 
			event.getUser().getUserName(), 
			event.getSource());
		
		// TODO: Send welcome email
		// TODO: Create default profile if needed
		// TODO: Send notification
	}

	/**
	 * Handles RoleAssignedEvent asynchronously.
	 * Can be extended to create role-specific profiles (e.g., doctor profile), send notifications, etc.
	 */
	@Async
	@EventListener
	public void handleRoleAssigned(RoleAssignedEvent event) {
		logger.info("Role assigned event received: userId={}, oldRole={}, newRole={}", 
			event.getUser().getUserId(),
			event.getOldRole() != null ? event.getOldRole().getRoleName() : "none",
			event.getNewRole().getRoleName());
		
		// TODO: Create role-specific profiles (e.g., doctor profile when DOCTOR role is assigned)
		// TODO: Send notification to user
		// TODO: Update user permissions
	}
}

