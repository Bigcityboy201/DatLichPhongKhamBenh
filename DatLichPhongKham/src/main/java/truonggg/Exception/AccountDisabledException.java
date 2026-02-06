package truonggg.Exception;

import truonggg.reponse.ErrorCode;

public class AccountDisabledException extends BusinessException {

	public AccountDisabledException() {
		super("User account is disabled", ErrorCode.ACCOUNT_DISABLED);
	}
}