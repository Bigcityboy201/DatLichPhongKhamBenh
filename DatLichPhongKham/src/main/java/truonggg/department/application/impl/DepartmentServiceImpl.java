package truonggg.department.application.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import truonggg.Exception.NotFoundException;
import truonggg.department.domain.model.Departments;
import truonggg.dto.reponseDTO.DepartmentsResponseDTO;
import truonggg.dto.requestDTO.DepartmentsRequestDTO;
import truonggg.dto.requestDTO.DepartmentsUpdateRequestDTO;
import truonggg.department.mapper.DepartmentsMapper;
import truonggg.department.infrastructure.DepartmentsRepository;
import truonggg.reponse.PagedResult;
import truonggg.department.application.DepartmentsCommandService;
import truonggg.department.application.DepartmentsQueryService;

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
		//Departments departments = this.departmentsMapper.toEntity(dto);
        Departments departments=Departments.create(dto.getName(), dto.getDescription());
		return this.departmentsMapper.toResponse(this.departmentsRepository.save(departments));
	}

	@Override
	@Transactional
	public DepartmentsResponseDTO update(Integer id, DepartmentsUpdateRequestDTO dto) {
		Departments found = getDepartmentOrThrow(id);
		found.changeInfo(dto.getName(), dto.getDescription());
		return this.departmentsMapper.toResponse(this.departmentsRepository.save(found));
	}

	@Override
	@Transactional
	public DepartmentsResponseDTO delete(Integer id, DepartmentsUpdateRequestDTO dto) {
		Departments found = getDepartmentOrThrow(id);
        Boolean active = dto.getActive();

        if (active == null) {
            throw new IllegalArgumentException("Active status is required");
        }

        if (active) {
            found.activate();
        } else {
            found.deactivate();
        }
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
}
