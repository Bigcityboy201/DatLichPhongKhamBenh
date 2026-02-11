package truonggg.mapper;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import truonggg.Model.Appointments;
import truonggg.Model.DoctorSpecializations;
import truonggg.Model.Doctors;
import truonggg.Model.Schedules;
import truonggg.dto.reponseDTO.AppointmentsResponseDTO;
import truonggg.dto.reponseDTO.DoctorSummaryResponseDTO;
import truonggg.dto.reponseDTO.DoctorsReponseDTO;
import truonggg.dto.requestDTO.DoctorsRequestDTO;

@Mapper(componentModel = "spring")
public interface DoctorsMapper {

	@Mapping(source = "user.userId", target = "userId")
	@Mapping(source = "user.fullName", target = "fullName")
	@Mapping(source = "user.email", target = "email")
	@Mapping(source = "user.phone", target = "phone")
	@Mapping(source = "departments.id", target = "departmentId")
	@Mapping(source = "departments.name", target = "departmentName")
	@Mapping(source = "isActive", target = "active")
	@Mapping(source = "isFeatured", target = "isFeatured")
	@Mapping(target = "specializations", expression = "java(mapSpecializations(doctors.getList4()))")
	@Mapping(target = "schedules", expression = "java(mapSchedules(doctors.getList2()))")
	@Mapping(target = "appointments", expression = "java(mapAppointments(doctors.getList()))")
	DoctorsReponseDTO toDTO(Doctors doctors);

	@Mapping(source = "user.userId", target = "userId")
	@Mapping(source = "user.fullName", target = "fullName")
	@Mapping(source = "user.email", target = "email")
	@Mapping(source = "user.phone", target = "phone")
	@Mapping(source = "departments.id", target = "departmentId")
	@Mapping(source = "departments.name", target = "departmentName")
	@Mapping(source = "isActive", target = "active")
	@Mapping(source = "isFeatured", target = "isFeatured")
	DoctorSummaryResponseDTO toDTOOther(Doctors doctors);

	default List<String> mapSpecializations(List<DoctorSpecializations> specializations) {
		if (specializations == null || specializations.isEmpty())
			return List.of();
		return specializations.stream().map(ds -> ds.getSpecializations().getSpecName()).collect(Collectors.toList());
	}

	default List<AppointmentsResponseDTO> mapAppointments(List<Appointments> appointments) {
		if (appointments == null || appointments.isEmpty()) {
			return List.of();
		}

		return appointments.stream().map(a -> {
			AppointmentsResponseDTO dto = new AppointmentsResponseDTO();
			// Thông tin cơ bản
			dto.setId(a.getId());
			dto.setAppointmentDateTime(a.getAppointmentDateTime());
			dto.setStatus(a.getStatus());
			dto.setNote(a.getNote());

			// Thông tin User
			if (a.getUser() != null) {
				dto.setUserId(a.getUser().getUserId());
				dto.setUserFullName(a.getUser().getFullName());
				dto.setUserEmail(a.getUser().getEmail());
				dto.setUserPhone(a.getUser().getPhone());
			}

			// Thông tin Bác sĩ
			if (a.getDoctors() != null) {
				dto.setDoctorId(a.getDoctors().getId());
				dto.setDoctorFullName(a.getDoctors().getUser().getFullName());
				dto.setDoctorDepartmentName(
						a.getDoctors().getDepartments() != null ? a.getDoctors().getDepartments().getName() : null);
				dto.setDoctorImageUrl(a.getDoctors().getImageUrl());
				dto.setDoctorExperienceYears(a.getDoctors().getExperienceYears());
			}

			return dto;
		}).collect(Collectors.toList());
	}

	default List<String> mapSchedules(List<Schedules> schedules) {
		if (schedules == null || schedules.isEmpty()) {
			return List.of();
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

		return schedules.stream().filter(s -> !s.getIsActive()) // active = false theo quy ước của bạn
				.filter(s -> s.getStartAt() != null && s.getEndAt() != null)
				.map(s -> s.getStartAt().format(formatter) + " - " + s.getEndAt().format(formatter))
				.collect(Collectors.toList());
	}

	default List<DoctorsReponseDTO> toDTOList(List<Doctors> doctorsList) {
		if (doctorsList == null || doctorsList.isEmpty())
			return List.of();
		return doctorsList.stream().map(this::toDTO).collect(Collectors.toList());
	}

	default List<DoctorSummaryResponseDTO> toDTOOtherList(List<Doctors> doctorsList) {
		if (doctorsList == null || doctorsList.isEmpty())
			return List.of();
		return doctorsList.stream().map(this::toDTOOther).collect(Collectors.toList());
	}

	Doctors toEntity(DoctorsRequestDTO dto);
}
