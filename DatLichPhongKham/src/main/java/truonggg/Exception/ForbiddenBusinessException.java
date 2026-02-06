package truonggg.Exception;

import truonggg.reponse.ErrorCode;

public class ForbiddenBusinessException extends BusinessException {

	public ForbiddenBusinessException(String message) {
		super(message, ErrorCode.FORBIDDEN);
	}
}
