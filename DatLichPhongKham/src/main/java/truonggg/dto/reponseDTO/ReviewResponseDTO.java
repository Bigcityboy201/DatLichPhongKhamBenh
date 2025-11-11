package truonggg.dto.reponseDTO;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDTO {
	private Integer id;
	private Integer rating;
	private String comment;
	private Date createAt;
	private Boolean active;
	private String userName;
	private String doctorName;
}
