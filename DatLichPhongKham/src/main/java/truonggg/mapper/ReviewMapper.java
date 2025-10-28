package truonggg.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import truonggg.Model.review;
import truonggg.dto.reponseDTO.ReviewResponseDTO;
import truonggg.dto.requestDTO.ReviewRequestDTO;
import truonggg.dto.requestDTO.ReviewUpdateRequestDTO;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ReviewMapper {
	
	@Mapping(target = "id", source = "id")
	@Mapping(target = "rating", source = "rating")
	@Mapping(target = "comment", source = "comment")
	@Mapping(target = "createAt", source = "createAt")
	@Mapping(target = "isActive", ignore = true)
	@Mapping(target = "userName", source = "user.fullName")
	@Mapping(target = "doctorName", source = "doctors.user.fullName")
	ReviewResponseDTO toDTO(review review);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createAt", ignore = true)
	@Mapping(target = "isActive", ignore = true)
	@Mapping(target = "user", ignore = true)
	@Mapping(target = "doctors", ignore = true)
	review toEntity(ReviewRequestDTO dto);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createAt", ignore = true)
	@Mapping(target = "isActive", ignore = true)
	@Mapping(target = "user", ignore = true)
	@Mapping(target = "doctors", ignore = true)
	review toEntity(ReviewUpdateRequestDTO dto);
}
