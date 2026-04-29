package truonggg.utils;

import java.util.regex.Pattern;

/**
 * Utility class for common validation operations.
 * Provides methods to validate email, phone number, and other common formats.
 */
public final class ValidationUtils {

	private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
	private static final String PHONE_PATTERN = "^[0-9]{10,11}$"; // Vietnamese phone: 10-11 digits
	private static final Pattern EMAIL_REGEX = Pattern.compile(EMAIL_PATTERN);
	private static final Pattern PHONE_REGEX = Pattern.compile(PHONE_PATTERN);

	private ValidationUtils() {
		// Utility class - prevent instantiation
	}

	/**
	 * Validates email format.
	 * 
	 * @param email the email to validate
	 * @return true if email is valid, false otherwise
	 */
	public static boolean isValidEmail(String email) {
		if (email == null || email.isBlank()) {
			return false;
		}
		return EMAIL_REGEX.matcher(email.trim()).matches();
	}

	/**
	 * Validates phone number format (Vietnamese: 10-11 digits).
	 * 
	 * @param phone the phone number to validate
	 * @return true if phone is valid, false otherwise
	 */
	public static boolean isValidPhone(String phone) {
		if (phone == null || phone.isBlank()) {
			return false;
		}
		// Remove spaces, dashes, and parentheses
		String cleaned = phone.replaceAll("[\\s\\-\\(\\)]", "");
		return PHONE_REGEX.matcher(cleaned).matches();
	}

	/**
	 * Validates that a string is not null or blank.
	 * 
	 * @param value the string to validate
	 * @return true if string is not null and not blank, false otherwise
	 */
	public static boolean isNotBlank(String value) {
		return value != null && !value.isBlank();
	}

	/**
	 * Validates that a string is not null or empty.
	 * 
	 * @param value the string to validate
	 * @return true if string is not null and not empty, false otherwise
	 */
	public static boolean isNotEmpty(String value) {
		return value != null && !value.isEmpty();
	}

	/**
	 * Validates string length is within range.
	 * 
	 * @param value the string to validate
	 * @param minLength minimum length (inclusive)
	 * @param maxLength maximum length (inclusive)
	 * @return true if length is within range, false otherwise
	 */
	public static boolean isLengthInRange(String value, int minLength, int maxLength) {
		if (value == null) {
			return false;
		}
		int length = value.length();
		return length >= minLength && length <= maxLength;
	}

	/**
	 * Validates that a number is within range.
	 * 
	 * @param value the number to validate
	 * @param min minimum value (inclusive)
	 * @param max maximum value (inclusive)
	 * @return true if value is within range, false otherwise
	 */
	public static boolean isInRange(int value, int min, int max) {
		return value >= min && value <= max;
	}

	/**
	 * Validates that a number is within range.
	 * 
	 * @param value the number to validate
	 * @param min minimum value (inclusive)
	 * @param max maximum value (inclusive)
	 * @return true if value is within range, false otherwise
	 */
	public static boolean isInRange(double value, double min, double max) {
		return value >= min && value <= max;
	}
}

