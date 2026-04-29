package truonggg.constant;

/**
 * Constants for validation rules.
 * Centralizes validation constraints to avoid magic numbers and strings.
 */
public final class ValidationConstants {

	private ValidationConstants() {
		// Utility class - prevent instantiation
	}

	// Password constraints
//	public static final int MIN_PASSWORD_LENGTH = 8;
//	public static final int MAX_PASSWORD_LENGTH = 50;
//
//	// Username constraints
//	public static final int MIN_USERNAME_LENGTH = 3;
//	public static final int MAX_USERNAME_LENGTH = 50;

	// Name constraints
	public static final int MIN_NAME_LENGTH = 2;
	public static final int MAX_NAME_LENGTH = 100;

	// Email constraints
	public static final int MAX_EMAIL_LENGTH = 255;

	// Phone constraints
	public static final int MIN_PHONE_LENGTH = 10;
	public static final int MAX_PHONE_LENGTH = 11; // Vietnamese phone

	// Address constraints
	public static final int MAX_ADDRESS_LENGTH = 500;

	// Description constraints
	public static final int MAX_DESCRIPTION_LENGTH = 1000;

	// Review constraints
	public static final int MIN_REVIEW_LENGTH = 10;
	public static final int MAX_REVIEW_LENGTH = 500;

	// Note constraints
	public static final int MAX_NOTE_LENGTH = 500;

	// Pagination constraints
	public static final int DEFAULT_PAGE_SIZE = 10;
	public static final int MAX_PAGE_SIZE = 100;
	public static final int MIN_PAGE_SIZE = 1;

	// Numeric constraints
	public static final double MIN_RATING = 1.0;
	public static final double MAX_RATING = 5.0;
	public static final int MIN_EXPERIENCE_YEARS = 0;
	public static final int MAX_EXPERIENCE_YEARS = 100;
}

