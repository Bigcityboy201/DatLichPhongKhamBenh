package truonggg.domain.event;

import truonggg.user.domain.model.User;

/**
 * Domain event published when a new user is created.
 * This event can be used to trigger side effects like sending welcome emails,
 * creating default profiles, etc.
 */
public class UserCreatedEvent {

	private final User user;
	private final String source; // e.g., "SIGNUP", "ADMIN_CREATE"

	public UserCreatedEvent(User user, String source) {
		this.user = user;
		this.source = source;
	}

	public User getUser() {
		return user;
	}

	public String getSource() {
		return source;
	}
}

