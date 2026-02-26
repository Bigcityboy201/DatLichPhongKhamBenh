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

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}