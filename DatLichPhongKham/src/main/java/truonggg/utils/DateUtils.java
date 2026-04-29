package truonggg.utils;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date and time operations.
 * Provides methods to convert between different date/time types and format dates.
 */
public final class DateUtils {

	private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private DateUtils() {
		// Utility class - prevent instantiation
	}

	/**
	 * Converts java.sql.Date to LocalDate.
	 * 
	 * @param date the sql Date to convert
	 * @return LocalDate representation, or null if input is null
	 */
	public static LocalDate toLocalDate(Date date) {
		if (date == null) {
			return null;
		}
		return date.toLocalDate();
	}

	/**
	 * Converts LocalDate to java.sql.Date.
	 * 
	 * @param localDate the LocalDate to convert
	 * @return sql Date representation, or null if input is null
	 */
	public static Date toSqlDate(LocalDate localDate) {
		if (localDate == null) {
			return null;
		}
		return Date.valueOf(localDate);
	}

	/**
	 * Converts LocalDateTime to java.sql.Date (truncates time part).
	 * 
	 * @param dateTime the LocalDateTime to convert
	 * @return sql Date representation, or null if input is null
	 */
	public static Date toSqlDate(LocalDateTime dateTime) {
		if (dateTime == null) {
			return null;
		}
		return Date.valueOf(dateTime.toLocalDate());
	}

	/**
	 * Formats LocalDateTime to string using default format (yyyy-MM-dd HH:mm:ss).
	 * 
	 * @param dateTime the LocalDateTime to format
	 * @return formatted string, or null if input is null
	 */
	public static String formatDateTime(LocalDateTime dateTime) {
		if (dateTime == null) {
			return null;
		}
		return dateTime.format(DEFAULT_DATETIME_FORMATTER);
	}

	/**
	 * Formats LocalDate to string using default format (yyyy-MM-dd).
	 * 
	 * @param date the LocalDate to format
	 * @return formatted string, or null if input is null
	 */
	public static String formatDate(LocalDate date) {
		if (date == null) {
			return null;
		}
		return date.format(DEFAULT_DATE_FORMATTER);
	}

	/**
	 * Formats LocalDateTime to string using custom format.
	 * 
	 * @param dateTime the LocalDateTime to format
	 * @param pattern the date format pattern
	 * @return formatted string, or null if input is null
	 */
	public static String formatDateTime(LocalDateTime dateTime, String pattern) {
		if (dateTime == null) {
			return null;
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		return dateTime.format(formatter);
	}

	/**
	 * Gets current LocalDateTime.
	 * 
	 * @return current LocalDateTime
	 */
	public static LocalDateTime now() {
		return LocalDateTime.now();
	}

	/**
	 * Gets current LocalDate.
	 * 
	 * @return current LocalDate
	 */
	public static LocalDate today() {
		return LocalDate.now();
	}

	/**
	 * Converts LocalDateTime to java.util.Date.
	 * 
	 * @param dateTime the LocalDateTime to convert
	 * @return java.util.Date representation, or null if input is null
	 */
	public static java.util.Date toUtilDate(LocalDateTime dateTime) {
		if (dateTime == null) {
			return null;
		}
		return java.util.Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Converts LocalDate to java.util.Date.
	 * 
	 * @param date the LocalDate to convert
	 * @return java.util.Date representation, or null if input is null
	 */
	public static java.util.Date toUtilDate(LocalDate date) {
		if (date == null) {
			return null;
		}
		return java.util.Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Gets current java.util.Date.
	 * 
	 * @return current java.util.Date
	 */
	public static java.util.Date currentDate() {
		return new java.util.Date();
	}
}

