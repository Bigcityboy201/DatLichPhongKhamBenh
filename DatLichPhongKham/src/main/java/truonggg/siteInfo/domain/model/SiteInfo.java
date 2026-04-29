package truonggg.siteInfo.domain.model;

import jakarta.persistence.*;
import lombok.*;
import truonggg.domain.model.BaseEntity;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteInfo extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String infoKey;
    private String value;
    @Column(columnDefinition = "BIT DEFAULT 1")
    private boolean isActive;

    // ===== Domain behaviour =====

    public static SiteInfo create(String infoKey, String value) {
        if (!truonggg.utils.ValidationUtils.isNotBlank(infoKey)) {
            throw new IllegalArgumentException("Info key is required");
        }
        SiteInfo siteInfo = new SiteInfo();
        siteInfo.infoKey = infoKey;
        siteInfo.value = value;
        siteInfo.isActive = true;
        return siteInfo;
    }

    public void updateInfo(String newKey, String newValue) {
        if (truonggg.utils.ValidationUtils.isNotBlank(newKey)) {
            this.infoKey = newKey;
        }
        if (newValue != null) {
            this.value = newValue;
        }
    }

    public void activate() {
        if (this.isActive) {
            return;
        }
        this.isActive = true;
    }

    public void deactivate() {
        if (!this.isActive) {
            return;
        }
        this.isActive = false;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}