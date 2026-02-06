package truonggg.Exception;

import truonggg.reponse.ErrorCode;

public class AccountLockedException extends BusinessException {

	public AccountLockedException() {
		super("User account is locked", ErrorCode.ACCOUNT_LOCKED);
	}
}