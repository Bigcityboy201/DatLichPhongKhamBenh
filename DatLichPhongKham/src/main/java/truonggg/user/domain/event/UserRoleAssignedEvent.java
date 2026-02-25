package truonggg.user.domain.event;

import jakarta.persistence.criteria.CriteriaBuilder;

public class UserRoleAssignedEvent {

    private final Integer userId;
    private final String roleName;

    public UserRoleAssignedEvent(Integer userId, String roleName) {
        this.userId = userId;
        this.roleName = roleName;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getRoleName() {
        return roleName;
    }

    public boolean isDoctor() {
        return "DOCTOR".equalsIgnoreCase(roleName);
    }
}