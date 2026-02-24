package truonggg.siteInfo.mapper;

import org.mapstruct.Mapper;
import truonggg.siteInfo.domain.model.SiteInfo;
import truonggg.dto.reponseDTO.SiteInfoResponseDTO;
import truonggg.dto.requestDTO.SiteInfoRequestDTO;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SiteInfoMapper {
    SiteInfoResponseDTO toDTO(final SiteInfo siteInfo);

    SiteInfo toModel(final SiteInfoRequestDTO dto);

    default List<SiteInfoResponseDTO> toDTOList(final List<SiteInfo> list) {
        if (list == null || list.isEmpty())
            return List.of();
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }
}