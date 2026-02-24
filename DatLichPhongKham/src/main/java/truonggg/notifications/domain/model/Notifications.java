package truonggg.notifications.domain.model;

import jakarta.persistence.*;
import lombok.*;
import truonggg.user.domain.model.User;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notifications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String message;
    @Column(columnDefinition = "BIT DEFAULT 0")
    private boolean isRead;// 1=da doc
    private Date createdAt;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private User user;

    // Thêm method thủ công cho boolean isRead
    public boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }
}

