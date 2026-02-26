package truonggg.appointment.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import truonggg.appointment.domain.model.Appointments;
import truonggg.dto.reponseDTO.AppointmentsResponseDTO;
import truonggg.dto.requestDTO.AppointmentsRequestDTO;

@Mapper(componentModel = "spring")
public interface AppointmentsMapper {

	@Mapping(source = "user.userId", target = "userId")
	@Mapping(source = "user.fullName", target = "userFullName")
	@Mapping(source = "user.email", target = "userEmail")
	@Mapping(source = "user.phone", target = "userPhone")
	@Mapping(source = "doctors.id", target = "doctorId")
	@Mapping(source = "doctors.user.fullName", target = "doctorFullName")
	@Mapping(source = "doctors.departments.name", target = "doctorDepartmentName")
	@Mapping(source = "doctors.imageUrl", target = "doctorImageUrl")
	@Mapping(source = "doctors.experienceYears", target = "doctorExperienceYears")
	@Mapping(source = "status", target = "status")
	AppointmentsResponseDTO toDTO(final Appointments appointments);

	// Không set trực tiếp doctors.id, sẽ gán Doctors trong service
	@Mapping(target = "doctors", ignore = true)
	@Mapping(target = "status", expression = "java(truonggg.Enum.Appointments_Enum.PENDING)")
	Appointments toEntity(AppointmentsRequestDTO dto);

	default List<AppointmentsResponseDTO> toDTOList(List<Appointments> appointments) {
		if (appointments == null || appointments.isEmpty())
			return List.of();
		return appointments.stream().map(this::toDTO).collect(Collectors.toList());
	}
}


