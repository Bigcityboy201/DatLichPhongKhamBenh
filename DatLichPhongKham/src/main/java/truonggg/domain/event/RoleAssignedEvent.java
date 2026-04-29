package truonggg.domain.event;

import truonggg.role.domain.model.Role;
import truonggg.user.domain.model.User;

/**
 * Domain event published when a role is assigned to a user.
 * This event can be used to trigger side effects like sending notifications,
 * creating default profiles (e.g., doctor profile when DOCTOR role is assigned), etc.
 */
public class RoleAssignedEvent {

	private final User user;
	private final Role newRole;
	private final Role oldRole; // null if this is the first role assignment

	public RoleAssignedEvent(User user, Role newRole, Role oldRole) {
		this.user = user;
		this.newRole = newRole;
		this.oldRole = oldRole;
	}

	public User getUser() {
		return user;
	}

	public Role getNewRole() {
		return newRole;
	}

	public Role getOldRole() {
		return oldRole;
	}
}

