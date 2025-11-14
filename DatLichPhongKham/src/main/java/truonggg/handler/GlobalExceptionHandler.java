package truonggg.handler;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;
import truonggg.Exception.NotFoundException;
import truonggg.Exception.UserAlreadyExistException;
import truonggg.reponse.ErrorCode;
import truonggg.reponse.ErrorReponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	@ExceptionHandler(exception = { NotFoundException.class })
	public ErrorReponse handle(final NotFoundException ex) {
		return ErrorReponse.of(ex.getMessage(), ex.getErrorCode(), ex.getDomain());
	}

	@ResponseStatus(value = HttpStatus.FORBIDDEN)
	@ExceptionHandler(exception = { AccessDeniedException.class })
	public ErrorReponse handle(final AccessDeniedException ex) {
		return ErrorReponse.of("You do not have permission to access this resource", ErrorCode.INVALID, "security");
	}

	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(exception = { AuthenticationException.class })
	public ErrorReponse handle(final AuthenticationException ex) {
		return ErrorReponse.of("Please login to access this resource", ErrorCode.INVALID, "security");
	}

	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ExceptionHandler(exception = { MethodArgumentNotValidException.class })
	public ErrorReponse handle(final MethodArgumentNotValidException exception) {
		Map<String, Object> details = exception.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(x -> x.getField().toString(), x -> x.getDefaultMessage(), (a, b) -> b));

		return ErrorReponse.of("Validation failed", ErrorCode.INVALID, "request", details);

	}

	@ResponseStatus(HttpStatus.CONFLICT) // 409 trùng dữ liệu
	@ExceptionHandler(UserAlreadyExistException.class)
	public ErrorReponse handleUserAlreadyExist(UserAlreadyExistException ex) {
		Map<String, Object> details = Map.of("field", ex.getMessage().contains("userName") ? "userName" : "email");

		return ErrorReponse.of(ex.getMessage(), ex.getErrorCode(), ex.getDomain(), details);
	}

	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ExceptionHandler(exception = { ConstraintViolationException.class })
	public ErrorReponse handle(final ConstraintViolationException exception) {
		Map<String, Object> details = exception.getConstraintViolations().stream()
				.collect(Collectors.toMap(x -> x.getPropertyPath().toString(), x -> x.getMessage(), (a, b) -> b));

		return ErrorReponse.of("Validation failed", ErrorCode.INVALID, "request", details);
	}

	// Lỗi database constraint (unique, foreign key, etc.)
	@ResponseStatus(value = HttpStatus.CONFLICT)
	@ExceptionHandler(exception = { DataIntegrityViolationException.class })
	public ErrorReponse handle(final DataIntegrityViolationException ex) {
		String message = ex.getMessage();
		if (message != null && message.contains("Duplicate entry")) {
			message = "Data already exists in the system";
		} else if (message != null && message.contains("foreign key constraint")) {
			message = "Cannot delete or update due to foreign key constraint";
		} else {
			message = "Data integrity violation: " + (message != null ? message : "Unknown error");
		}
		return ErrorReponse.of(message, ErrorCode.INVALID, "database");
	}

	// Request body không đọc được (JSON malformed, missing body, etc.)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ExceptionHandler(exception = { HttpMessageNotReadableException.class })
	public ErrorReponse handle(final HttpMessageNotReadableException ex) {
		String message = ex.getMessage();
		if (message != null && message.contains("JSON parse error")) {
			message = "Invalid JSON format in request body";
		} else if (message != null && message.contains("Required request body is missing")) {
			message = "Request body is required but missing";
		} else {
			message = "Invalid request body format";
		}
		return ErrorReponse.of(message, ErrorCode.INVALID, "request");
	}

	// Method HTTP không được hỗ trợ
	@ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(exception = { HttpRequestMethodNotSupportedException.class })
	public ErrorReponse handle(final HttpRequestMethodNotSupportedException ex) {
		return ErrorReponse.of(
				"HTTP method '" + ex.getMethod() + "' is not supported for this endpoint",
				ErrorCode.INVALID,
				"request");
	}

	// Thiếu required parameter
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ExceptionHandler(exception = { MissingServletRequestParameterException.class })
	public ErrorReponse handle(final MissingServletRequestParameterException ex) {
		return ErrorReponse.of(
				"Required parameter '" + ex.getParameterName() + "' is missing",
				ErrorCode.INVALID,
				"request");
	}

	// Type mismatch (ví dụ: truyền string vào Integer)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ExceptionHandler(exception = { MethodArgumentTypeMismatchException.class })
	public ErrorReponse handle(final MethodArgumentTypeMismatchException ex) {
		return ErrorReponse.of(
				"Parameter '" + ex.getName() + "' has invalid type. Expected: " + ex.getRequiredType().getSimpleName(),
				ErrorCode.INVALID,
				"request");
	}

	// Tham số không hợp lệ
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ExceptionHandler(exception = { IllegalArgumentException.class })
	public ErrorReponse handle(final IllegalArgumentException ex) {
		return ErrorReponse.of(ex.getMessage() != null ? ex.getMessage() : "Invalid argument provided",
				ErrorCode.INVALID,
				"request");
	}

	// Media type không được hỗ trợ
	@ResponseStatus(value = HttpStatus.UNSUPPORTED_MEDIA_TYPE)
	@ExceptionHandler(exception = { HttpMediaTypeNotSupportedException.class })
	public ErrorReponse handle(final HttpMediaTypeNotSupportedException ex) {
		return ErrorReponse.of(
				"Media type '" + ex.getContentType() + "' is not supported",
				ErrorCode.INVALID,
				"request");
	}

	// Binding error
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ExceptionHandler(exception = { BindException.class })
	public ErrorReponse handle(final BindException ex) {
		Map<String, Object> details = ex.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(x -> x.getField().toString(), x -> x.getDefaultMessage(), (a, b) -> b));

		return ErrorReponse.of("Binding validation failed", ErrorCode.INVALID, "request", details);
	}

	// NullPointerException - Tránh lộ thông tin chi tiết
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(exception = { NullPointerException.class })
	public ErrorReponse handle(final NullPointerException ex) {
		return ErrorReponse.of("An internal error occurred. Please contact support if the problem persists",
				ErrorCode.INTERNAL_SERVER,
				"system");
	}

	// Handler cuối cùng - bắt tất cả exception còn lại
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(exception = { Exception.class })
	public ErrorReponse handle(final Exception ex) {
		// Log exception để debug (có thể thêm logger sau)
		ex.printStackTrace();
		return ErrorReponse.of(
				ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred",
				ErrorCode.INTERNAL_SERVER,
				"system");
	}
}
