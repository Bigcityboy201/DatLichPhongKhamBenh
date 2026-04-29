package truonggg.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import truonggg.utils.ValidationUtils;

public class ValidPhoneValidator implements ConstraintValidator<ValidPhone, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // để @NotBlank xử lý nếu cần
        }
        return ValidationUtils.isValidPhone(value);
    }
}
