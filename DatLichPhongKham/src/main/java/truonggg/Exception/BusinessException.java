package truonggg.Exception;

import lombok.Getter;
import truonggg.reponse.ErrorCode;

@Getter
public abstract class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;
	private final String domain;

	protected BusinessException(String message, ErrorCode errorCode) {
		super(message);
		this.errorCode = errorCode;
		this.domain = "business";
	}
}
