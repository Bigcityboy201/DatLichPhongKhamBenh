package truonggg.Exception;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import truonggg.reponse.ErrorCode;

@Getter
@Setter
public class UserAlreadyExistException extends MultiFieldViolationException {

	private static final long serialVersionUID = 1L;

	// Multi-field
	public UserAlreadyExistException(Map<String, String> errors) {
		super("user", errors, ErrorCode.ALREADY_EXIST);
	}

	// Single-field
	public UserAlreadyExistException(String field, String message) {
		super("user", Map.of(field, message), ErrorCode.ALREADY_EXIST);
	}
}
