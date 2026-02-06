package truonggg.Exception;

import truonggg.reponse.ErrorCode;

public class NotFoundException extends BusinessException {

	public NotFoundException(String resource, String message) {
		super(resource + ": " + message, ErrorCode.NOT_FOUND);
	}
}
