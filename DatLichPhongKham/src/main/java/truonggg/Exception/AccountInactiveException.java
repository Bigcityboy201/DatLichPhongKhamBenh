package truonggg.Exception;

import truonggg.reponse.ErrorCode;

public class AccountInactiveException extends BusinessException {

	public AccountInactiveException() {
		super("User account is inactive", ErrorCode.ACCOUNT_INACTIVE);
	}
}