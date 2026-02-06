# Exception handling & Global handler rules (DatLichPhongKham)

Tài liệu này ghi lại **nguyên tắc dùng Exception**, và **vì sao `GlobalExceptionHandler` bắt được lỗi** trong Spring Boot.

## Vì sao GlobalExceptionHandler “bắt được” exception?

Project đang dùng:

- `@RestControllerAdvice` trên `truonggg.handler.GlobalExceptionHandler`
- `@ExceptionHandler(...)` cho từng loại exception

Trong Spring MVC, khi một request vào controller/service ném exception (không bị catch), DispatcherServlet sẽ:

- tìm trong các `@ControllerAdvice/@RestControllerAdvice` các method `@ExceptionHandler` phù hợp theo **kiểu exception**
- nếu match, nó dùng method đó để tạo `ResponseEntity` trả về client
- nếu không match, sẽ rơi về handler tổng quát hoặc default error handling

File chính:
- `src/main/java/truonggg/handler/GlobalExceptionHandler.java`

Ngoài ra, phần **Spring Security** có luồng riêng (filter chain). Các lỗi “403 do security” có thể đi qua:
- `truonggg.sercurity.CustomAccessDeniedHandler` (AccessDenied trong security filter)
- hoặc đi qua `GlobalExceptionHandler` (nếu bạn tự `throw AccessDeniedException` trong service/controller)

## Quy ước: nên throw exception nào trong trường hợp nào?

### Nhóm Business exception (custom)

Các exception kế thừa `BusinessException` sẽ được `GlobalExceptionHandler` map status theo `ErrorCode`:

- `NotFoundException` → `ErrorCode.NOT_FOUND` → **404**
  - dùng khi resource không tồn tại (user/doctor/appointment/payment…)
- `ConflictException` → `ErrorCode.CONFLICT` → **409**
  - dùng khi xung đột dữ liệu (trùng dữ liệu, trạng thái không cho phép…)
- `ForbiddenBusinessException` → `ErrorCode.FORBIDDEN` → **403**
  - dùng khi “nghiệp vụ” cấm (không phải do security token)
- `BadRequestException` → `ErrorCode.BAD_REQUEST` → **400**
  - dùng khi request hợp lệ về format nhưng sai về nghiệp vụ/input
- `AccountInactiveException/AccountLockedException/AccountDisabledException`
  - hiện đang được map về **400** trong handler

**Nguyên tắc**: Business exception nên mang thông điệp rõ ràng, ổn định, và không lộ stacktrace ra client.

### Nhóm Validation đa field

- `MultiFieldViolationException` (và subclass `UserAlreadyExistException`)
  - trả về **409** + `details` chứa map lỗi theo field
  - dùng khi cần báo nhiều lỗi cùng lúc: `{"email": "...", "userName": "..." }`

Lưu ý: `UserAlreadyExistException` đã được chuẩn hóa dùng `ErrorCode.ALREADY_EXIST` (không dùng `ALREADY_EXIT` vì deprecated).

### Nhóm exception Java/Spring thông dụng

Trong code hiện tại có nhiều chỗ dùng:

- `IllegalArgumentException` → nên trả **400**
  - ví dụ: phương thức thanh toán không hợp lệ, thời gian đặt lịch không hợp lệ…
- `IllegalStateException` → thường hợp lý trả **409**
  - ví dụ: appointment đã thanh toán rồi (xung đột trạng thái)
- `org.springframework.security.access.AccessDeniedException` → nên trả **403**
  - ví dụ: user không có quyền thao tác trên resource

Các mapping này đã được thêm vào `GlobalExceptionHandler` để tránh rơi về 500.

## Bảng mapping hiện tại (HTTP)

- **400 Bad Request**
  - `MethodArgumentNotValidException`, `ConstraintViolationException`, `BindException`
  - `HttpMessageNotReadableException`, `MissingServletRequestParameterException`, `MethodArgumentTypeMismatchException`
  - `IllegalArgumentException`
  - `BusinessException` với `ErrorCode.BAD_REQUEST`, `ACCOUNT_INACTIVE`, `ACCOUNT_LOCKED`, `ACCOUNT_DISABLED`

- **403 Forbidden**
  - `AccessDeniedException`
  - `BusinessException` với `ErrorCode.FORBIDDEN`

- **404 Not Found**
  - `BusinessException` với `ErrorCode.NOT_FOUND`

- **409 Conflict**
  - `MultiFieldViolationException`
  - `DataIntegrityViolationException`
  - `IllegalStateException`
  - `BusinessException` với `ErrorCode.CONFLICT` hoặc `ALREADY_EXIST`

- **415 Unsupported Media Type**
  - `HttpMediaTypeNotSupportedException`

- **405 Method Not Allowed**
  - `HttpRequestMethodNotSupportedException` (status 405, message code hiện là `BAD_REQUEST`)

- **500 Internal Server Error**
  - tất cả exception chưa được map riêng

## Gợi ý best-practice (nhẹ, không bắt buộc)

- Ưu tiên custom exception (`BusinessException` / `MultiFieldViolationException`) cho nghiệp vụ để response đồng nhất.
- `AccessDeniedException` nên dành cho quyền truy cập (403).
- `IllegalArgumentException` cho input sai (400), `IllegalStateException` cho xung đột trạng thái (409).
- Không trả stacktrace ra client; log stacktrace ở server (hiện handler đang comment log).


