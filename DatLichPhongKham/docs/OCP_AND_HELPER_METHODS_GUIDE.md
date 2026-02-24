# Hướng Dẫn về OCP và Tổ Chức Helper Methods

## 1. Open/Closed Principle (OCP) là gì?

**Open/Closed Principle** là một trong 5 nguyên tắc SOLID:
- **Open for Extension**: Code phải mở rộng được (thêm tính năng mới) mà không cần sửa code cũ
- **Closed for Modification**: Code đã tồn tại không nên bị sửa đổi

### Vi phạm OCP thường gặp:

1. **Switch/Case statements** với nhiều case
2. **If-else chains** dài với nhiều điều kiện
3. **Method có nhiều trách nhiệm** xử lý nhiều loại đối tượng khác nhau

---

## 2. Ví Dụ Vi Phạm OCP trong Codebase

### 2.1. GlobalExceptionHandler - Switch Statement

**File:** `src/main/java/truonggg/handler/GlobalExceptionHandler.java`

```java
@ExceptionHandler(BusinessException.class)
public ResponseEntity<ErrorReponse> handleBusinessException(BusinessException ex) {
    HttpStatus status = switch (ex.getErrorCode()) {
        case NOT_FOUND -> HttpStatus.NOT_FOUND;
        case CONFLICT, ALREADY_EXIST -> HttpStatus.CONFLICT;
        case FORBIDDEN -> HttpStatus.FORBIDDEN;
        case BAD_REQUEST, ACCOUNT_INACTIVE, ACCOUNT_LOCKED, ACCOUNT_DISABLED -> HttpStatus.BAD_REQUEST;
        default -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
    // ...
}
```

**Vấn đề:** Mỗi khi thêm ErrorCode mới, phải sửa method này.

**Giải pháp:** Sử dụng Strategy Pattern hoặc Map-based approach.

### 2.2. UserServiceIMPL - If-else cho Role

**File:** `src/main/java/truonggg/service/user/impl/UserServiceIMPL.java`

```java
if (role.getRoleName().equalsIgnoreCase("DOCTOR")) {
    doctorsRepository.findByUser(finalUser).orElseGet(() -> {
        // Tạo doctor entity
    });
}
```

**Vấn đề:** Nếu thêm role mới (ví dụ: NURSE), phải sửa method này.

**Giải pháp:** Sử dụng Strategy Pattern hoặc Factory Pattern.

### 2.3. AppointmentServiceImpl - Nhiều điều kiện status

**File:** `src/main/java/truonggg/service/appointment/impl/AppointmentServiceImpl.java`

```java
if (found.getStatus() == Appointments_Enum.COMPLETED || 
    found.getStatus() == Appointments_Enum.CANCELLED ||
    found.getStatus() == Appointments_Enum.CANCELLED_NO_REFUND ||
    found.getStatus() == Appointments_Enum.CANCELLED_REFUND) {
    throw new IllegalArgumentException("Không thể hủy lịch hẹn ở trạng thái hiện tại");
}
```

**Vấn đề:** Logic kiểm tra status rải rác, khó maintain.

**Giải pháp:** Tạo Status Validator hoặc State Pattern.

---

## 3. Các Cách Tổ Chức Helper Methods

### 3.1. Private Methods trong ServiceImpl ✅ (Khi nào dùng)

**Nên dùng khi:**
- Helper method chỉ được sử dụng trong **một ServiceImpl duy nhất**
- Logic **gắn chặt** với business logic của service đó
- Không cần **reuse** ở service khác
- Method **ngắn gọn**, không quá phức tạp

**Ví dụ tốt từ codebase:**

```java
// PaymentServiceIMPL.java
private PaymentMethod resolvePaymentMethod(String method) {
    if (method == null || method.isBlank()) {
        return PaymentMethod.BANK_TRANSFER;
    }
    try {
        return PaymentMethod.valueOf(method.toUpperCase());
    } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ");
    }
}

private void validatePermission(User user, Appointments appointment) {
    boolean isAdminOrEmployee = user.getRole() != null 
        && Boolean.FALSE.equals(user.getRole().getIsActive())
        && ("ADMIN".equals(user.getRole().getRoleName()) 
            || "EMPLOYEE".equals(user.getRole().getRoleName()));
    
    if (!isAdminOrEmployee && !appointment.getUser().getUserId().equals(user.getUserId())) {
        throw new AccessDeniedException("Bạn không có quyền thanh toán cho appointment này");
    }
}
```

**Ưu điểm:**
- ✅ Dễ tìm (cùng file với business logic)
- ✅ Encapsulation tốt (không expose ra ngoài)
- ✅ Không tạo thêm file

**Nhược điểm:**
- ❌ Không thể reuse ở service khác
- ❌ File ServiceImpl có thể dài nếu có quá nhiều helper

---

### 3.2. Utility Classes (Static Helper Classes) ✅ (Khi nào dùng)

**Nên dùng khi:**
- Helper method **không phụ thuộc** vào state của service
- Cần **reuse** ở nhiều service khác nhau
- Logic **độc lập**, không gắn với business logic cụ thể
- Method là **pure function** (input → output, không side effect)

**Cấu trúc đề xuất:**

```
src/main/java/truonggg/utils/
├── JwtUtils.java (đã có)
├── PaymentUtils.java
├── AppointmentUtils.java
├── ValidationUtils.java
└── DateTimeUtils.java
```

**Ví dụ:**

```java
// PaymentUtils.java
package truonggg.utils;

import truonggg.Enum.PaymentMethod;

public final class PaymentUtils {
    
    private PaymentUtils() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static PaymentMethod resolvePaymentMethod(String method) {
        if (method == null || method.isBlank()) {
            return PaymentMethod.BANK_TRANSFER;
        }
        try {
            return PaymentMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ");
        }
    }
    
    public static String normalizePaymentContent(String content) {
        return content == null ? null : content.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
    }
}
```

**Sử dụng:**

```java
// PaymentServiceIMPL.java
import truonggg.utils.PaymentUtils;

PaymentMethod method = PaymentUtils.resolvePaymentMethod(dto.getPaymentMethod());
```

**Ưu điểm:**
- ✅ Reusable ở nhiều nơi
- ✅ Dễ test (static method)
- ✅ Tổ chức code rõ ràng

**Nhược điểm:**
- ❌ Khó mock trong test (nếu cần)
- ❌ Không thể inject dependencies

---

### 3.3. Helper Service Classes (Component/Service) ✅ (Khi nào dùng)

**Nên dùng khi:**
- Helper method cần **inject dependencies** (Repository, Service khác)
- Logic phức tạp, cần **state management**
- Cần **reuse** ở nhiều service nhưng có dependencies
- Muốn **test dễ dàng** (có thể mock)

**Cấu trúc đề xuất:**

```
src/main/java/truonggg/service/helper/
├── PaymentValidationHelper.java
├── AppointmentValidationHelper.java
├── UserPermissionHelper.java
└── StatusTransitionHelper.java
```

**Ví dụ:**

```java
// PaymentValidationHelper.java
package truonggg.service.helper;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import truonggg.Model.User;
import truonggg.Model.Appointments;
import truonggg.repo.UserRepository;
import org.springframework.security.access.AccessDeniedException;

@Component
@RequiredArgsConstructor
public class PaymentValidationHelper {
    
    private final UserRepository userRepository;
    
    public void validatePaymentPermission(User user, Appointments appointment) {
        boolean isAdminOrEmployee = isAdminOrEmployee(user);
        
        if (!isAdminOrEmployee && !appointment.getUser().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("Bạn không có quyền thanh toán cho appointment này");
        }
    }
    
    public boolean isAdminOrEmployee(User user) {
        return user.getRole() != null 
            && Boolean.FALSE.equals(user.getRole().getIsActive())
            && ("ADMIN".equals(user.getRole().getRoleName()) 
                || "EMPLOYEE".equals(user.getRole().getRoleName()));
    }
}
```

**Sử dụng:**

```java
// PaymentServiceIMPL.java
@Service
@RequiredArgsConstructor
public class PaymentServiceIMPL implements PaymentService {
    
    private final PaymentValidationHelper validationHelper;
    
    public PaymentResponseDTO createPayment(PaymentRequestDTO dto, String username) {
        // ...
        validationHelper.validatePaymentPermission(user, appointment);
        // ...
    }
}
```

**Ưu điểm:**
- ✅ Có thể inject dependencies
- ✅ Dễ test (mock được)
- ✅ Reusable
- ✅ Có thể có state

**Nhược điểm:**
- ❌ Tạo thêm file
- ❌ Cần quản lý dependencies

---

### 3.4. Strategy Pattern ✅ (Đã có trong codebase)

**Đã áp dụng tốt:** `PaymentStrategy` pattern

**File:** `src/main/java/truonggg/strategy/PaymentStrategy.java`

```java
public interface PaymentStrategy {
    PaymentMethod getSupportedMethod();
    Payments processPayment(Appointments appointment, PaymentRequestDTO dto, User user);
}
```

**Ưu điểm:**
- ✅ Tuân thủ OCP hoàn toàn
- ✅ Dễ mở rộng (thêm strategy mới không cần sửa code cũ)
- ✅ Single Responsibility

**Áp dụng cho các trường hợp khác:**
- ErrorCode → HttpStatus mapping (thay cho switch)
- Role-based actions (thay cho if-else)
- Status validation (thay cho nhiều if-else)

---

## 4. Quyết Định: Nên Dùng Cách Nào?

### Decision Tree:

```
Helper method cần gì?
│
├─ Chỉ dùng trong 1 ServiceImpl?
│  └─ YES → Private method trong ServiceImpl ✅
│
├─ Cần reuse ở nhiều nơi?
│  │
│  ├─ Có dependencies (Repository, Service)?
│  │  └─ YES → Helper Service Class (Component) ✅
│  │
│  └─ Không có dependencies (pure function)?
│     └─ YES → Utility Class (static) ✅
│
└─ Logic phức tạp, nhiều case khác nhau?
   └─ YES → Strategy Pattern ✅
```

### Ví Dụ Cụ Thể:

| Method | Nơi hiện tại | Nên chuyển thành | Lý do |
|--------|-------------|------------------|-------|
| `resolvePaymentMethod()` | PaymentServiceIMPL (private) | **Utility Class** | Pure function, có thể reuse |
| `validatePermission()` | PaymentServiceIMPL (private) | **Helper Service** | Cần check role, có thể reuse |
| `isAdminOrEmployee()` | PaymentServiceIMPL (private) | **Helper Service** | Logic phức tạp, nhiều nơi dùng |
| `parseAppointmentIdFromContent()` | PaymentServiceIMPL (private) | **Utility Class** | Pure function, parsing logic |
| `normalize()` | PaymentServiceIMPL (private) | **Utility Class** | Pure function, string manipulation |
| ErrorCode → HttpStatus mapping | GlobalExceptionHandler (switch) | **Strategy Pattern** | Nhiều case, dễ mở rộng |

---

## 5. Best Practices

### 5.1. Naming Conventions

- **Utility Classes:** `*Utils.java` (ví dụ: `PaymentUtils`, `DateTimeUtils`)
- **Helper Services:** `*Helper.java` hoặc `*Validator.java` (ví dụ: `PaymentValidationHelper`)
- **Private Methods:** Tên mô tả rõ ràng (ví dụ: `validateAppointmentTime`, `convertToLocalDateTime`)

### 5.2. Package Structure

```
src/main/java/truonggg/
├── utils/              # Static utility classes
│   ├── PaymentUtils.java
│   ├── DateTimeUtils.java
│   └── ValidationUtils.java
│
├── service/
│   ├── helper/         # Helper services (Component)
│   │   ├── PaymentValidationHelper.java
│   │   └── AppointmentValidationHelper.java
│   │
│   └── impl/           # Service implementations
│       └── PaymentServiceIMPL.java
│
└── strategy/           # Strategy pattern (đã có)
    ├── PaymentStrategy.java
    └── impl/
        └── CashPaymentStrategy.java
```

### 5.3. Khi Nào Refactor?

**Nên refactor khi:**
- ✅ Method được **copy-paste** ở nhiều service
- ✅ Private method **quá dài** (> 20 lines)
- ✅ Logic có **nhiều if-else/switch** (> 3 cases)
- ✅ Cần **test riêng** helper method
- ✅ Method **không phụ thuộc** vào state của service

**Không cần refactor khi:**
- ❌ Method chỉ dùng **1 lần**, logic đơn giản
- ❌ Method **gắn chặt** với business logic của service
- ❌ Refactor không mang lại lợi ích rõ ràng

---

## 6. Ví Dụ Refactor Cụ Thể

### 6.1. Refactor: ErrorCode → HttpStatus Mapping

**Trước (vi phạm OCP):**

```java
// GlobalExceptionHandler.java
HttpStatus status = switch (ex.getErrorCode()) {
    case NOT_FOUND -> HttpStatus.NOT_FOUND;
    case CONFLICT, ALREADY_EXIST -> HttpStatus.CONFLICT;
    case FORBIDDEN -> HttpStatus.FORBIDDEN;
    case BAD_REQUEST, ACCOUNT_INACTIVE, ACCOUNT_LOCKED, ACCOUNT_DISABLED -> HttpStatus.BAD_REQUEST;
    default -> HttpStatus.INTERNAL_SERVER_ERROR;
};
```

**Sau (tuân thủ OCP - Strategy Pattern):**

```java
// ErrorCodeToHttpStatusMapper.java
@Component
public class ErrorCodeToHttpStatusMapper {
    
    private final Map<ErrorCode, HttpStatus> statusMap;
    
    public ErrorCodeToHttpStatusMapper() {
        this.statusMap = Map.of(
            ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND,
            ErrorCode.CONFLICT, HttpStatus.CONFLICT,
            ErrorCode.ALREADY_EXIST, HttpStatus.CONFLICT,
            ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN,
            ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST,
            ErrorCode.ACCOUNT_INACTIVE, HttpStatus.BAD_REQUEST,
            ErrorCode.ACCOUNT_LOCKED, HttpStatus.BAD_REQUEST,
            ErrorCode.ACCOUNT_DISABLED, HttpStatus.BAD_REQUEST
        );
    }
    
    public HttpStatus map(ErrorCode errorCode) {
        return statusMap.getOrDefault(errorCode, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

**Sử dụng:**

```java
// GlobalExceptionHandler.java
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    
    private final ErrorCodeToHttpStatusMapper statusMapper;
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorReponse> handleBusinessException(BusinessException ex) {
        HttpStatus status = statusMapper.map(ex.getErrorCode());
        return ResponseEntity.status(status).body(...);
    }
}
```

**Lợi ích:**
- ✅ Thêm ErrorCode mới chỉ cần thêm vào Map, không sửa method
- ✅ Dễ test
- ✅ Tách biệt logic mapping

---

### 6.2. Refactor: isAdminOrEmployee() - Từ Private → Helper Service

**Trước:**

```java
// PaymentServiceIMPL.java
private boolean isAdminOrEmployee(User user) {
    return user.getRole() != null 
        && Boolean.FALSE.equals(user.getRole().getIsActive())
        && ("ADMIN".equals(user.getRole().getRoleName()) 
            || "EMPLOYEE".equals(user.getRole().getRoleName()));
}
```

**Sau:**

```java
// UserPermissionHelper.java
@Component
public class UserPermissionHelper {
    
    public boolean isAdminOrEmployee(User user) {
        if (user.getRole() == null) {
            return false;
        }
        
        if (Boolean.TRUE.equals(user.getRole().getIsActive())) {
            return false; // Role không active
        }
        
        String roleName = user.getRole().getRoleName();
        return "ADMIN".equals(roleName) || "EMPLOYEE".equals(roleName);
    }
    
    public boolean isDoctor(User user) {
        return user.getRole() != null 
            && Boolean.FALSE.equals(user.getRole().getIsActive())
            && "DOCTOR".equals(user.getRole().getRoleName());
    }
}
```

**Sử dụng:**

```java
// PaymentServiceIMPL.java
@RequiredArgsConstructor
public class PaymentServiceIMPL implements PaymentService {
    
    private final UserPermissionHelper permissionHelper;
    
    private void validatePermission(User user, Appointments appointment) {
        if (!permissionHelper.isAdminOrEmployee(user) 
            && !appointment.getUser().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("Bạn không có quyền thanh toán cho appointment này");
        }
    }
}
```

---

## 7. Tổng Kết

### Quy Tắc Vàng:

1. **Private methods trong ServiceImpl**: Dùng khi logic chỉ thuộc về service đó, không cần reuse
2. **Utility Classes**: Dùng cho pure functions, không dependencies, cần reuse
3. **Helper Services**: Dùng khi có dependencies, logic phức tạp, cần reuse
4. **Strategy Pattern**: Dùng khi có nhiều case khác nhau, cần mở rộng dễ dàng

### Checklist Khi Quyết Định:

- [ ] Method có được dùng ở nhiều service không?
- [ ] Method có cần dependencies (Repository, Service) không?
- [ ] Logic có nhiều if-else/switch không?
- [ ] Method có thể là pure function không?
- [ ] Có cần test riêng method này không?

---

## 8. Tài Liệu Tham Khảo

- [SOLID Principles - OCP](https://en.wikipedia.org/wiki/Open%E2%80%93closed_principle)
- [Strategy Pattern](https://refactoring.guru/design-patterns/strategy)
- [Clean Code - Helper Methods](https://www.amazon.com/Clean-Code-Handbook-Software-Craftsmanship/dp/0132350882)

---

**Tác giả:** Generated for DatLichPhongKham Project  
**Ngày:** 2025  
**Version:** 1.0


