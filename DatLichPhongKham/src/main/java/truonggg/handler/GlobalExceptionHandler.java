package truonggg.handler;

//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;
import org.springframework.security.access.AccessDeniedException;
import truonggg.Exception.BusinessException;
import truonggg.Exception.MultiFieldViolationException;
import truonggg.reponse.ErrorCode;
import truonggg.reponse.ErrorReponse;

//@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorReponse> handleBusinessException(BusinessException ex) {

		HttpStatus status = switch (ex.getErrorCode()) {
		case NOT_FOUND -> HttpStatus.NOT_FOUND;
		case CONFLICT, ALREADY_EXIST -> HttpStatus.CONFLICT;
		case FORBIDDEN -> HttpStatus.FORBIDDEN;
		case BAD_REQUEST, ACCOUNT_INACTIVE, ACCOUNT_LOCKED, ACCOUNT_DISABLED -> HttpStatus.BAD_REQUEST;
		default -> HttpStatus.INTERNAL_SERVER_ERROR;
		};

		return ResponseEntity.status(status).body(ErrorReponse.of(ex.getMessage(), ex.getErrorCode(), ex.getDomain()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorReponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {

		Map<String, Object> details = ex.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(field -> field.getField(), field -> field.getDefaultMessage(), (a, b) -> b));

		return ResponseEntity.badRequest()
				.body(ErrorReponse.of("Validation failed", ErrorCode.BAD_REQUEST, "request", details));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorReponse> handleConstraintViolation(ConstraintViolationException ex) {

		Map<String, Object> details = ex.getConstraintViolations().stream()
				.collect(Collectors.toMap(v -> v.getPropertyPath().toString(), v -> v.getMessage(), (a, b) -> b));

		return ResponseEntity.badRequest()
				.body(ErrorReponse.of("Validation failed", ErrorCode.BAD_REQUEST, "request", details));
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<ErrorReponse> handleBindException(BindException ex) {

		Map<String, Object> details = ex.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(field -> field.getField(), field -> field.getDefaultMessage(), (a, b) -> b));

		return ResponseEntity.badRequest()
				.body(ErrorReponse.of("Binding validation failed", ErrorCode.BAD_REQUEST, "request", details));
	}

	@ExceptionHandler(MultiFieldViolationException.class)
	public ResponseEntity<ErrorReponse> handleMultiFieldViolation(MultiFieldViolationException ex) {

		Map<String, Object> details = ex.getFieldErrors().entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> (Object) e.getValue()));

		return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorReponse
				.of("Validation failed for fields: " + details.keySet(), ex.getErrorCode(), ex.getDomain(), details));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorReponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {

		return ResponseEntity.badRequest()
				.body(ErrorReponse.of("Invalid request body format", ErrorCode.BAD_REQUEST, "request"));
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorReponse> handleMissingParameter(MissingServletRequestParameterException ex) {

		return ResponseEntity.badRequest().body(ErrorReponse
				.of("Required parameter '" + ex.getParameterName() + "' is missing", ErrorCode.BAD_REQUEST, "request"));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorReponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

		return ResponseEntity.badRequest().body(
				ErrorReponse.of("Parameter '" + ex.getName() + "' has invalid type", ErrorCode.BAD_REQUEST, "request"));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorReponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {

		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ErrorReponse
				.of("HTTP method '" + ex.getMethod() + "' is not supported", ErrorCode.BAD_REQUEST, "request"));
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ErrorReponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {

		return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
				.body(ErrorReponse.of("Media type is not supported", ErrorCode.BAD_REQUEST, "request"));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorReponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {

		// log.warn("DataIntegrityViolationException", ex);

		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(ErrorReponse.of("Data integrity violation", ErrorCode.CONFLICT, "database"));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorReponse> handleIllegalArgument(IllegalArgumentException ex) {
		return ResponseEntity.badRequest()
				.body(ErrorReponse.of(ex.getMessage(), ErrorCode.BAD_REQUEST, "request"));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ErrorReponse> handleIllegalState(IllegalStateException ex) {
		// Thường dùng cho xung đột trạng thái (ví dụ: đã thanh toán rồi)
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(ErrorReponse.of(ex.getMessage(), ErrorCode.CONFLICT, "business"));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorReponse> handleAccessDenied(AccessDeniedException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(ErrorReponse.of(ex.getMessage(), ErrorCode.FORBIDDEN, "security"));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorReponse> handleUnhandledException(Exception ex) {

		// log.error("Unhandled exception", ex);
		if (ex instanceof org.springframework.security.core.AuthenticationException) {
			throw (org.springframework.security.core.AuthenticationException) ex;
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorReponse
				.of("An internal error occurred. Please contact support.", ErrorCode.INTERNAL_SERVER_ERROR, "system"));
	}
}
