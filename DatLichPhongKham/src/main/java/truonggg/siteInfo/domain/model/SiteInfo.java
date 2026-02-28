package truonggg.siteInfo.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String infoKey;
    private String value;
    @Column(columnDefinition = "BIT DEFAULT 1")
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== Domain behaviour =====

    public static SiteInfo create(String infoKey, String value) {
        if (infoKey == null || infoKey.isBlank()) {
            throw new IllegalArgumentException("Info key is required");
        }
        SiteInfo siteInfo = new SiteInfo();
        siteInfo.infoKey = infoKey;
        siteInfo.value = value;
        siteInfo.isActive = true;
        siteInfo.createdAt = LocalDateTime.now();
        siteInfo.updatedAt = siteInfo.createdAt;
        return siteInfo;
    }

    public void updateInfo(String newKey, String newValue) {
        if (newKey != null && !newKey.isBlank()) {
            this.infoKey = newKey;
        }
        if (newValue != null) {
            this.value = newValue;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        if (this.isActive) {
            return;
        }
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        if (!this.isActive) {
            return;
        }
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}