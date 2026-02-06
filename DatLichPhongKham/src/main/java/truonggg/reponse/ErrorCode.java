package truonggg.reponse;

public enum ErrorCode {

	// ===== SUCCESS =====
	OK,

	// ===== COMMON / LEGACY (GIỮ LẠI) =====
	INVALID, // legacy – hạn chế dùng mới
	MAX, // legacy
	MIN, // legacy

	// ===== 4xx – CLIENT / BUSINESS =====
	BAD_REQUEST, // dữ liệu không hợp lệ
	NOT_FOUND, // không tìm thấy resource
	CONFLICT, // trùng dữ liệu
	ALREADY_EXIST, // thay thế ALREADY_EXIT
	FORBIDDEN, // bị cấm (business hoặc security)
	UNAUTHORIZED, // chưa đăng nhập / token sai

	// ===== ACCOUNT =====
	ACCOUNT_INACTIVE, ACCOUNT_LOCKED, ACCOUNT_DISABLED,

	// ===== 5xx – SERVER =====
	INTERNAL_SERVER_ERROR,

	// ===== DEPRECATED (KHÔNG DÙNG MỚI) =====
	@Deprecated
	ALREADY_EXIT,

	@Deprecated
	INTERNAL_SERVER
}
