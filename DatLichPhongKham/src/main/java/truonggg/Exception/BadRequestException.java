package truonggg.Exception;

import truonggg.reponse.ErrorCode;

public class BadRequestException extends BusinessException {

	public BadRequestException(String message) {
		super(message, ErrorCode.BAD_REQUEST);
	}
}
