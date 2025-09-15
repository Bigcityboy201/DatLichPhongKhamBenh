package truonggg.Model;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Notifications {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String message;
	private boolean IsRead;// 1=da doc
	private Date createdAt;
	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "UserId")
	private User user;
}
