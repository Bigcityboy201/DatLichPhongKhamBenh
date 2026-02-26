package truonggg.role.application;

import truonggg.constant.SecurityRole;
import truonggg.user.domain.model.User;

public interface RoleAssignmentHandler {
    String supportedRole();

    void onAssigned(User user);

    void onRemoved(User user);
}