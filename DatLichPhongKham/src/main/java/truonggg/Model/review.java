package truonggg.Model;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class review {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private int rating;
	private String comment;
	private Date createAt;
	private boolean isActive;
	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "UserId")
	private User user;
	@ManyToOne
	@JoinColumn(name = "doctors_id", referencedColumnName = "id")
	private Doctors doctors;
}
