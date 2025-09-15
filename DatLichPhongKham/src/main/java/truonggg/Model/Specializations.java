package truonggg.Model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Specializations {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String specName;
	private String description;
	private boolean isActive;
	@OneToMany(mappedBy = "specializations")
	private List<DoctorSpecializations> list = new ArrayList<>();
}
