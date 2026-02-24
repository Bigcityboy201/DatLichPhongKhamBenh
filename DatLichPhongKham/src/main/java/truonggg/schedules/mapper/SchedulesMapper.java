package truonggg.schedules.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import truonggg.schedules.domain.model.Schedules;
import truonggg.dto.reponseDTO.SchedulesReponseDTO;
import truonggg.dto.requestDTO.SchedulesRequestDTO;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SchedulesMapper {
    @Mapping(source = "doctors.id", target = "doctorId")
    @Mapping(source = "doctors.user.fullName", target = "doctorName")
    @Mapping(source = "isActive", target = "active")
    SchedulesReponseDTO toDTO(final Schedules dto);

    @Mapping(source = "doctorId", target = "doctors.id")
    Schedules toModel(final SchedulesRequestDTO dto);

    default List<SchedulesReponseDTO> toDTOList(List<Schedules> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }
}