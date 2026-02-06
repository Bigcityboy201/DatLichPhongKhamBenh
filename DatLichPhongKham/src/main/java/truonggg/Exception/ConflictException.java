package truonggg.Exception;

import truonggg.reponse.ErrorCode;

public class ConflictException extends BusinessException {

	public ConflictException(String message) {
		super(message, ErrorCode.CONFLICT);
	}
}
