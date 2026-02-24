package truonggg.role.application.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import truonggg.Exception.NotFoundException;
import truonggg.role.domain.model.Role;
import truonggg.dto.reponseDTO.RoleResponseDTO;
import truonggg.dto.requestDTO.RoleDeleteRequestDTO;
import truonggg.dto.requestDTO.RoleRequestDTO;
import truonggg.dto.requestDTO.RoleUpdateRequestDTO;
import truonggg.role.mapper.RoleMapper;
import truonggg.role.infrastructure.RoleRepository;
import truonggg.reponse.PagedResult;
import truonggg.role.application.RoleCommandService;
import truonggg.role.application.RoleQueryService;

@Service
@RequiredArgsConstructor
public class RoleServiceIMPL implements RoleQueryService, RoleCommandService {

	private final RoleRepository roleRepository;
	private final RoleMapper roleMapper;

	@Override
	public RoleResponseDTO createRole(RoleRequestDTO dto) {
		Role role = this.roleMapper.toEntity(dto);
		role.setIsActive(true);
		role = this.roleRepository.save(role);
		RoleResponseDTO response = this.roleMapper.toDTO(role);
		response.setDescription(role.getDescription());
		response.setIsActive(role.getIsActive());
		return response;
	}

	@Override
	public PagedResult<RoleResponseDTO> getAll(Pageable pageable) {
		Page<Role> rolesPage = this.roleRepository.findAll(pageable);
		List<RoleResponseDTO> dtoList = rolesPage.stream().map(role -> {
			RoleResponseDTO dto = roleMapper.toDTO(role);
			dto.setDescription(role.getDescription());
			dto.setIsActive(role.getIsActive());
			return dto;
		}).collect(Collectors.toList());
		
		return PagedResult.from(rolesPage, dtoList);
	}

	@Override
	public RoleResponseDTO findById(Integer id) {
		Role role = this.roleRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("role", "Role Not Found"));
		RoleResponseDTO dto = this.roleMapper.toDTO(role);
		dto.setDescription(role.getDescription());
		dto.setIsActive(role.getIsActive());
		return dto;
	}

	@Override
	public RoleResponseDTO update(RoleUpdateRequestDTO dto) {
		Role foundRole = this.roleRepository.findById(dto.getRoleId())
				.orElseThrow(() -> new NotFoundException("role", "Role Not Found"));

		if (dto.getRoleName() != null) {
			foundRole.setRoleName(dto.getRoleName());
		}
		if (dto.getDescription() != null) {
			foundRole.setDescription(dto.getDescription());
		}

		Role savedRole = this.roleRepository.save(foundRole);
		RoleResponseDTO response = this.roleMapper.toDTO(savedRole);
		response.setDescription(savedRole.getDescription());
		response.setIsActive(savedRole.getIsActive());
		return response;
	}

	@Override
	public boolean delete(RoleDeleteRequestDTO dto) {
		Role foundRole = this.roleRepository.findById(dto.getRoleId())
				.orElseThrow(() -> new NotFoundException("role", "Role Not Found"));

		if (dto.getIsActive() != null) {
			foundRole.setIsActive(dto.getIsActive());
			this.roleRepository.save(foundRole);
		}
		return true;
	}

	@Override
	public boolean delete(Integer id) {
		Role foundRole = this.roleRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("role", "Role Not Found"));

		this.roleRepository.delete(foundRole);
		return true;
	}
}
