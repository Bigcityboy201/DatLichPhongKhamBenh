package truonggg.schedules.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import truonggg.schedules.domain.model.Schedules;
import truonggg.dto.reponseDTO.SchedulesReponseDTO;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SchedulesMapper {
    @Mapping(source = "doctors.id", target = "doctorId")
    @Mapping(source = "doctors.user.fullName", target = "doctorName")
    @Mapping(source = "isActive", target = "active")
    SchedulesReponseDTO toDTO(final Schedules dto);

    /**
     * Entity dùng enum {@code truonggg.Enum.DayOfWeek} nhưng response DTO dùng {@code java.time.DayOfWeek},
     * nên cần mapping thủ công để MapStruct hiểu cách convert.
     */
    default java.time.DayOfWeek map(final truonggg.Enum.DayOfWeek value) {
        return value == null ? null : java.time.DayOfWeek.valueOf(value.name());
    }

    default List<SchedulesReponseDTO> toDTOList(List<Schedules> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }
}