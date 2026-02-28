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

    // ===== Domain behaviour =====

    public static Notifications create(User user, String message) {
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }
        Notifications notification = new Notifications();
        notification.user = user;
        notification.message = message;
        notification.isRead = false;
        notification.createdAt = new Date();
        return notification;
    }

    public void markAsRead() {
        if (this.isRead) {
            return;
        }
        this.isRead = true;
    }

    public void updateMessage(String newMessage) {
        if (newMessage != null && !newMessage.isBlank()) {
            this.message = newMessage;
        }
    }

    // Thêm method thủ công cho boolean isRead (giữ lại cho JPA/Lombok tương thích)
    public boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }
}

