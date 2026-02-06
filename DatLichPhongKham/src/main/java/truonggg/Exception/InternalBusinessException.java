package truonggg.Exception;

import truonggg.reponse.ErrorCode;

public class InternalBusinessException extends BusinessException {

	public InternalBusinessException(String message) {
		super(message, ErrorCode.INTERNAL_SERVER_ERROR);
	}
}