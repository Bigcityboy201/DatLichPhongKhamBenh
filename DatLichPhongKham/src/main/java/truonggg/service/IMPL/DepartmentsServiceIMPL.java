package truonggg.service.IMPL;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import truonggg.Exception.NotFoundException;
import truonggg.Model.Departments;
import truonggg.dto.reponseDTO.DepartmentsResponseDTO;
import truonggg.dto.requestDTO.DepartmentsRequestDTO;
import truonggg.dto.requestDTO.DepartmentsUpdateRequestDTO;
import truonggg.mapper.DepartmentsMapper;
import truonggg.repo.DepartmentsRepository;
import truonggg.reponse.PagedResult;
import truonggg.service.DepartmentsService;

@Service
@RequiredArgsConstructor
public class DepartmentsServiceIMPL implements DepartmentsService {

	private final DepartmentsMapper departmentsMapper;

	private final DepartmentsRepository departmentsRepository;

	@Override
	public DepartmentsResponseDTO createDepartment(DepartmentsRequestDTO dto) {
		Departments departments = this.departmentsMapper.toEntity(dto);
		departments = this.departmentsRepository.save(departments);
		return this.departmentsMapper.toResponse(departments);
	}

	@Override
	public List<DepartmentsResponseDTO> getAll() {
		List<Departments> departments = this.departmentsRepository.findAll();
		return this.departmentsMapper.toDTOList(departments);
	}

	@Override
	public PagedResult<DepartmentsResponseDTO> getAllPaged(Pageable pageable) {
		Page<Departments> departmentsPage = departmentsRepository.findAll(pageable);

		List<DepartmentsResponseDTO> dtoList = departmentsMapper.toDTOList(departmentsPage.getContent());

		return PagedResult.<DepartmentsResponseDTO>builder().content(dtoList)
				.totalElements((int) departmentsPage.getTotalElements()).totalPages(departmentsPage.getTotalPages())
				.currentPage(departmentsPage.getNumber()).pageSize(departmentsPage.getSize()).build();
	}

	@Override
	public DepartmentsResponseDTO update(Integer id, DepartmentsUpdateRequestDTO dto) {
		// tìm xem có khoa không
		Departments foundDepartments = this.departmentsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("department", "Department Not Found "));
		// lấy ra
		if (dto.getName() != null) {
			foundDepartments.setName(dto.getName());
		}
		if (dto.getDescription() != null) {
			foundDepartments.setDescription(dto.getDescription());
		}
		return this.departmentsMapper.toResponse(this.departmentsRepository.save(foundDepartments));
	}

	@Override
	public DepartmentsResponseDTO delete(Integer id, DepartmentsUpdateRequestDTO dto) {
		// tìm xem có khoa không
		Departments foundDepartments = this.departmentsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("department", "Department Not Found "));
		// lấy ra
		if (dto.getActive() != null) {
			foundDepartments.setIsActive(dto.getActive());
		}
		return this.departmentsMapper.toResponse(foundDepartments);
	}

	@Override
	public boolean delete(Integer id) {
		// tìm xem có khoa không
		Departments foundDepartments = this.departmentsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("department", "Department Not Found "));
		this.departmentsRepository.delete(foundDepartments);
		return true;
	}

	@Override
	public PagedResult<DepartmentsResponseDTO> searchDepartments(String keyword, Pageable pageable) {
		Page<Departments> departmentPage = departmentsRepository.findByNameContainingIgnoreCase(keyword, pageable);

		// Chuyển đổi sang DTO
		List<DepartmentsResponseDTO> dtoList = departmentPage.getContent().stream().map(departmentsMapper::toResponse)
				.collect(Collectors.toList());

		// Trả về PagedResult dùng builder
		PagedResult<DepartmentsResponseDTO> pagedResult = PagedResult.<DepartmentsResponseDTO>builder().content(dtoList)
				.totalElements((int) departmentPage.getTotalElements()).totalPages(departmentPage.getTotalPages())
				.currentPage(departmentPage.getNumber()) // 0-based, cộng +1 nếu muốn 1-based
				.pageSize(departmentPage.getSize()).build();

		return pagedResult;
	}

}
