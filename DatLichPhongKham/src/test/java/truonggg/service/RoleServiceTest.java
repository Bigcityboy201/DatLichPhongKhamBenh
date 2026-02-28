package truonggg.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import truonggg.Exception.NotFoundException;
import truonggg.role.domain.model.Role;
import truonggg.dto.reponseDTO.RoleResponseDTO;
import truonggg.dto.requestDTO.RoleDeleteRequestDTO;
import truonggg.dto.requestDTO.RoleRequestDTO;
import truonggg.dto.requestDTO.RoleUpdateRequestDTO;
import truonggg.role.mapper.RoleMapper;
import truonggg.role.infrastructure.RoleRepository;
import truonggg.reponse.PagedResult;
import truonggg.role.application.impl.RoleServiceIMPL;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private RoleMapper roleMapper;

	@InjectMocks
	private RoleServiceIMPL roleService;

	@DisplayName("createRole: success")
	@Test
	void createRole_ShouldCreate() {
		RoleRequestDTO dto = new RoleRequestDTO();
		dto.setRoleName("ADMIN");
		Role saved = new Role();

		when(roleRepository.save(any(Role.class))).thenReturn(saved);
		when(roleMapper.toDTO(saved)).thenReturn(new RoleResponseDTO());

		RoleResponseDTO result = roleService.createRole(dto);

		assertNotNull(result);
		verify(roleRepository).save(any(Role.class));
	}

	@DisplayName("getAll: success")
	@Test
	void getAll_ShouldReturnPagedResult() {
		Pageable pageable = PageRequest.of(0, 2);
		Role r1 = new Role();
		Role r2 = new Role();

		Page<Role> page = new PageImpl<>(List.of(r1, r2), pageable, 2);

		when(roleRepository.findAll(pageable)).thenReturn(page);
		when(roleMapper.toDTO(any(Role.class))).thenReturn(new RoleResponseDTO());

		PagedResult<RoleResponseDTO> result = roleService.getAll(pageable);

		assertNotNull(result);
		assertEquals(2, result.getContent().size());
		verify(roleRepository).findAll(pageable);
	}

	@DisplayName("findById: throw NotFoundException when not found")
	@Test
	void findById_ShouldThrow_WhenNotFound() {
		when(roleRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> roleService.findById(1));
		assertEquals("role: Role Not Found", ex.getMessage());
	}

	@DisplayName("update: throw NotFoundException when not found")
	@Test
	void update_ShouldThrow_WhenNotFound() {
		RoleUpdateRequestDTO dto = new RoleUpdateRequestDTO();
		dto.setRoleId(1);

		when(roleRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> roleService.update(dto));
		assertEquals("role: Role Not Found", ex.getMessage());
	}

	@DisplayName("delete (soft): success when exists")
	@Test
	void deleteSoft_ShouldSucceed_WhenExists() {
		RoleDeleteRequestDTO dto = new RoleDeleteRequestDTO();
		dto.setRoleId(1);
		dto.setIsActive(false);

		Role role = new Role();

		when(roleRepository.findById(1)).thenReturn(Optional.of(role));
		when(roleRepository.save(role)).thenReturn(role);

		boolean result = roleService.delete(dto);

		assertTrue(result);
		assertEquals(false, role.getIsActive());
		verify(roleRepository).save(role);
	}

}


