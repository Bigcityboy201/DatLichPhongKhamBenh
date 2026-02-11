package truonggg.service.department.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import truonggg.Exception.NotFoundException;
import truonggg.Model.Departments;
import truonggg.dto.reponseDTO.DepartmentsResponseDTO;
import truonggg.dto.requestDTO.DepartmentsRequestDTO;
import truonggg.dto.requestDTO.DepartmentsUpdateRequestDTO;
import truonggg.mapper.DepartmentsMapper;
import truonggg.repo.DepartmentsRepository;
import truonggg.reponse.PagedResult;
import truonggg.service.department.DepartmentsCommandService;
import truonggg.service.department.DepartmentsQueryService;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentsCommandService, DepartmentsQueryService {
	private final DepartmentsMapper departmentsMapper;
	private final DepartmentsRepository departmentsRepository;

	// ================= QUERY =================

	@Override
	public PagedResult<DepartmentsResponseDTO> getAllPaged(Pageable pageable) {
		Page<Departments> departmentsPage = departmentsRepository.findAll(pageable);
		List<DepartmentsResponseDTO> dtoList = departmentsMapper.toDTOList(departmentsPage.getContent());

		return PagedResult.from(departmentsPage, dtoList);
	}

	@Override
	public DepartmentsResponseDTO findById(Integer id) {
		return this.departmentsMapper.toResponse(getDepartmentOrThrow(id));
	}

	@Override
	public PagedResult<DepartmentsResponseDTO> searchDepartments(String keyword, Pageable pageable) {
		Page<Departments> departmentPage = departmentsRepository.findByNameContainingIgnoreCase(keyword, pageable);
		List<DepartmentsResponseDTO> dtoList = departmentsMapper.toDTOList(departmentPage.getContent());

		return PagedResult.from(departmentPage, dtoList);
	}

	// ================= COMMAND =================

	@Override
	@Transactional
	public DepartmentsResponseDTO createDepartment(DepartmentsRequestDTO dto) {
		Departments departments = this.departmentsMapper.toEntity(dto);
		return this.departmentsMapper.toResponse(this.departmentsRepository.save(departments));
	}

	@Override
	@Transactional
	public DepartmentsResponseDTO update(Integer id, DepartmentsUpdateRequestDTO dto) {
		Departments found = getDepartmentOrThrow(id);
		applyUpdate(found, dto);
		return this.departmentsMapper.toResponse(this.departmentsRepository.save(found));
	}

	@Override
	@Transactional
	public DepartmentsResponseDTO delete(Integer id, DepartmentsUpdateRequestDTO dto) {
		Departments found = getDepartmentOrThrow(id);
		applyStatus(found, dto);
		return this.departmentsMapper.toResponse(this.departmentsRepository.save(found));
	}

	@Override
	@Transactional
	public boolean delete(Integer id) {
		Departments found = getDepartmentOrThrow(id);
		this.departmentsRepository.delete(found);
		return true;
	}

	// ================= INTERNAL =================

	private Departments getDepartmentOrThrow(Integer id) {
		return this.departmentsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("department", "Department Not Found"));
	}

	private void applyUpdate(Departments entity, DepartmentsUpdateRequestDTO dto) {
		if (dto.getName() != null) {
			entity.setName(dto.getName());
		}
		if (dto.getDescription() != null) {
			entity.setDescription(dto.getDescription());
		}
	}

	private void applyStatus(Departments entity, DepartmentsUpdateRequestDTO dto) {
		if (dto.getActive() != null) {
			entity.setIsActive(dto.getActive());
		}
	}
}
