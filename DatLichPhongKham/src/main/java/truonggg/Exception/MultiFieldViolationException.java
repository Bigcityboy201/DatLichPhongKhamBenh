package truonggg.Exception;

import java.util.Map;

import lombok.Getter;
import truonggg.reponse.ErrorCode;

@Getter
public class MultiFieldViolationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String domain;
	private final Map<String, String> fieldErrors;
	private final ErrorCode errorCode;

	public MultiFieldViolationException(String domain, Map<String, String> fieldErrors, ErrorCode errorCode) {
		super("Validation failed for fields: " + fieldErrors.keySet());
		this.domain = domain;
		this.fieldErrors = fieldErrors;
		this.errorCode = errorCode;
	}
}