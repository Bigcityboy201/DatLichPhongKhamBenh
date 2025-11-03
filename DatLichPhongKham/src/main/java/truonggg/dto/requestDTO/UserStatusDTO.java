package truonggg.dto.requestDTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UserStatusDTO {
    // Chấp nhận cả "active" và "isActive" từ request
    @JsonProperty("active")
    @JsonAlias({"isActive"})
    private Boolean active; // true = kích hoạt, false = vô hiệu
}
