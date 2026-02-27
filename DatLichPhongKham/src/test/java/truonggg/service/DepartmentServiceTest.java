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
import truonggg.department.domain.model.Departments;
import truonggg.department.mapper.DepartmentsMapper;
import truonggg.dto.reponseDTO.DepartmentsResponseDTO;
import truonggg.dto.requestDTO.DepartmentsRequestDTO;
import truonggg.dto.requestDTO.DepartmentsUpdateRequestDTO;
import truonggg.department.infrastructure.DepartmentsRepository;
import truonggg.reponse.PagedResult;
import truonggg.department.application.impl.DepartmentServiceImpl;

@ExtendWith(MockitoExtension.class)
public class DepartmentServiceTest {

	@Mock
	private DepartmentsMapper departmentsMapper;

	@Mock
	private DepartmentsRepository departmentsRepository;

	@InjectMocks
	private DepartmentServiceImpl departmentService;

	// ============= getAllPaged ============
	@DisplayName("getAllPaged: success")
	@Test
	void getAllPaged_ShouldReturnPagedResult() {
		Pageable pageable = PageRequest.of(0, 2);
		Departments d1 = new Departments();
		Departments d2 = new Departments();
		Page<Departments> page = new PageImpl<>(List.of(d1, d2), pageable, 2);

		when(departmentsRepository.findAll(pageable)).thenReturn(page);
		when(departmentsMapper.toDTOList(page.getContent())).thenReturn(List.of(new DepartmentsResponseDTO(),
				new DepartmentsResponseDTO()));

		PagedResult<DepartmentsResponseDTO> result = departmentService.getAllPaged(pageable);

		assertNotNull(result);
		assertEquals(2, result.getContent().size());
		verify(departmentsRepository).findAll(pageable);
	}

	// ============= findById ============
	@DisplayName("findById: success when department exists")
	@Test
	void findById_ShouldReturn_WhenExists() {
		Departments dep = new Departments();
		DepartmentsResponseDTO dto = new DepartmentsResponseDTO();

		when(departmentsRepository.findById(1)).thenReturn(Optional.of(dep));
		when(departmentsMapper.toResponse(dep)).thenReturn(dto);

		DepartmentsResponseDTO result = departmentService.findById(1);

		assertNotNull(result);
		verify(departmentsRepository).findById(1);
	}

	@DisplayName("findById: throw NotFoundException when not found")
	@Test
	void findById_ShouldThrow_WhenNotFound() {
		when(departmentsRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> departmentService.findById(1));
		assertEquals("department: Department Not Found", ex.getMessage());
	}

	// ============= createDepartment ============
	@DisplayName("createDepartment: success")
	@Test
	void createDepartment_ShouldReturnCreated() {
		DepartmentsRequestDTO dto = new DepartmentsRequestDTO();
		dto.setName("Khoa Nội");
		dto.setDescription("Mô tả");

		Departments saved = Departments.create(dto.getName(), dto.getDescription());
		DepartmentsResponseDTO responseDTO = new DepartmentsResponseDTO();

		when(departmentsRepository.save(any(Departments.class))).thenReturn(saved);
		when(departmentsMapper.toResponse(saved)).thenReturn(responseDTO);

		DepartmentsResponseDTO result = departmentService.createDepartment(dto);

		assertNotNull(result);
		verify(departmentsRepository).save(any(Departments.class));
	}

	// ============= update ============
	@DisplayName("update: success when department exists")
	@Test
	void update_ShouldApplyChanges_WhenExists() {
		Integer id = 1;
		Departments dep = Departments.create("Old", "Old desc");

		DepartmentsUpdateRequestDTO dto = new DepartmentsUpdateRequestDTO();
		dto.setName("New");

		when(departmentsRepository.findById(id)).thenReturn(Optional.of(dep));
		when(departmentsRepository.save(dep)).thenReturn(dep);
		when(departmentsMapper.toResponse(dep)).thenReturn(new DepartmentsResponseDTO());

		DepartmentsResponseDTO result = departmentService.update(id, dto);

		assertNotNull(result);
		assertEquals("New", dep.getName());
		verify(departmentsRepository).save(dep);
	}

	@DisplayName("update: throw NotFoundException when department not found")
	@Test
	void update_ShouldThrow_WhenNotFound() {
		DepartmentsUpdateRequestDTO dto = new DepartmentsUpdateRequestDTO();
		when(departmentsRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> departmentService.update(1, dto));
		assertEquals("department: Department Not Found", ex.getMessage());
	}

	// ============= delete soft ============
	@DisplayName("delete (soft): success when department exists")
	@Test
	void deleteSoft_ShouldUpdateStatus_WhenExists() {
		Integer id = 1;
		Departments dep = Departments.create("Khoa A", "desc"); // mặc định active=true

		DepartmentsUpdateRequestDTO dto = new DepartmentsUpdateRequestDTO();
		dto.setActive(false);

		when(departmentsRepository.findById(id)).thenReturn(Optional.of(dep));
		when(departmentsRepository.save(dep)).thenReturn(dep);
		when(departmentsMapper.toResponse(dep)).thenReturn(new DepartmentsResponseDTO());

		DepartmentsResponseDTO result = departmentService.delete(id, dto);

		assertNotNull(result);
		assertEquals(false, dep.getIsActive());
		verify(departmentsRepository).save(dep);
	}

	@DisplayName("delete (soft): throw NotFoundException when department not found")
	@Test
	void deleteSoft_ShouldThrow_WhenNotFound() {
		DepartmentsUpdateRequestDTO dto = new DepartmentsUpdateRequestDTO();
		dto.setActive(false);

		when(departmentsRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> departmentService.delete(1, dto));
		assertEquals("department: Department Not Found", ex.getMessage());
	}

	// ============= delete hard ============
	@DisplayName("delete (hard): success when department exists")
	@Test
	void deleteHard_ShouldDelete_WhenExists() {
		Departments dep = new Departments();
		when(departmentsRepository.findById(1)).thenReturn(Optional.of(dep));

		boolean result = departmentService.delete(1);

		assertTrue(result);
		verify(departmentsRepository).delete(dep);
	}

	@DisplayName("delete (hard): throw NotFoundException when department not found")
	@Test
	void deleteHard_ShouldThrow_WhenNotFound() {
		when(departmentsRepository.findById(1)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> departmentService.delete(1));
		assertEquals("department: Department Not Found", ex.getMessage());

		verify(departmentsRepository, never()).delete(any());
	}
}


