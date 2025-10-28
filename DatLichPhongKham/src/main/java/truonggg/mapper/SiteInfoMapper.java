package truonggg.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import truonggg.Model.SiteInfo;
import truonggg.dto.reponseDTO.SiteInfoResponseDTO;
import truonggg.dto.requestDTO.SiteInfoRequestDTO;

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
