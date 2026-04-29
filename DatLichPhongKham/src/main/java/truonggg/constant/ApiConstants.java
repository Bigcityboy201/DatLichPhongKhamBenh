package truonggg.constant;

/**
 * Constants for API endpoints. Centralizes all API path definitions to avoid
 * hard-coded strings.
 */
public final class ApiConstants {

	private ApiConstants() {
		// Utility class - prevent instantiation
	}

	// Base API path
	public static final String API_BASE = "/api";

	// Auth endpoints
	public static final String AUTH_BASE = API_BASE + "/auth";
	public static final String AUTH_SIGNUP = "/signup";
	public static final String AUTH_SIGNIN = "/signin";

	// User endpoints
	public static final String USERS_BASE = API_BASE + "/users";
	public static final String USERS_ME = "/me";
	public static final String USERS_ASSIGN_ROLE = "/assign-role";
	public static final String USERS_PROFILE = "/profile";
	public static final String USERS_STATUS = USERS_BASE + "/{id}/status";
	public static final String USERS_DELETE_MANUAL = "/manually/{id}";

	// Doctor endpoints
	public static final String DOCTORS_BASE = API_BASE + "/doctors";
	public static final String DOCTORS_ME = "/me";
	public static final String DOCTORS_DEPARTMENT = "/department";
	public static final String DOCTORS_SEARCH = "/search";
	public static final String DOCTORS_PROFILE = "/profile";

	// Department endpoints
	public static final String DEPARTMENTS_BASE = API_BASE + "/departments";
	public static final String DEPARTMENTS_SEARCH = "/search";
	public static final String DEPARTMENTS_STATUS = "/status/{id}";
	public static final String DEPARTMENTS_DELETE_MANUAL = "/manually/{id}";

	// Schedule endpoints
	public static final String SCHEDULES_BASE = API_BASE + "/schedules";
	public static final String SCHEDULES_DOCTOR = "/doctor/{doctorId}";
	public static final String SCHEDULES_STATUS = "/status/{id}";

	// Appointment endpoints
	public static final String APPOINTMENTS_BASE = API_BASE + "/appointments";
	public static final String APPOINTMENTS_ME = "/me";
	public static final String APPOINTMENTS_ASSIGN_DOCTOR = "/{id}/assign-doctor";
	public static final String APPOINTMENTS_CANCEL_USER = "/{id}/cancel-user";
	public static final String APPOINTMENTS_STATUS = "/{id}/status";

	// Payment endpoints
	public static final String PAYMENTS_BASE = API_BASE + "/payments";
	public static final String PAYMENTS_BANK_TRANSFER_CALLBACK = "/bank-transfer-callback";
	public static final String PAYMENTS_CASSO_WEBHOOK = "/casso-webhook";

	// Notification endpoints
	public static final String NOTIFICATIONS_BASE = API_BASE + "/notifications";
	public static final String NOTIFICATIONS_ME = "/me";
	public static final String NOTIFICATIONS_ME_UNREAD = NOTIFICATIONS_ME + "/unread";
	public static final String NOTIFICATIONS_ME_READ = NOTIFICATIONS_ME + "/{id}/read";
	public static final String NOTIFICATIONS_USER = "/user/{userId}";
	public static final String NOTIFICATIONS_USER_UNREAD = NOTIFICATIONS_USER + "/unread";

	// Review endpoints
	public static final String REVIEWS_BASE = API_BASE + "/reviews";
	public static final String REVIEWS_DOCTOR = "/doctor/{doctorId}";
	public static final String REVIEWS_ME = "/me";
	public static final String REVIEWS_STATUS = "/status/{id}";

	// Role endpoints
	public static final String ROLES_BASE = API_BASE + "/roles";

	// SiteInfo endpoints
	public static final String SITEINFOS_BASE = API_BASE + "/siteinfos";

	// Admin endpoints
	public static final String ADMIN_BASE = API_BASE + "/admin";
}
