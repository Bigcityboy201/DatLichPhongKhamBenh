package truonggg.service.IMPL;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import truonggg.Exception.NotFoundException;
import truonggg.Model.Departments;
import truonggg.Model.Doctors;
import truonggg.Model.User;
import truonggg.dto.reponseDTO.DoctorsReponseDTO;
import truonggg.dto.requestDTO.DoctorUpdateRequestDTO;
import truonggg.dto.requestDTO.DoctorsDeleteRequestDTO;
import truonggg.dto.requestDTO.DoctorsRequestDTO;
import truonggg.mapper.DoctorsMapper;
import truonggg.repo.DepartmentsRepository;
import truonggg.repo.DoctorsRepository;
import truonggg.repo.UserRepository;
import truonggg.reponse.PagedResult;
import truonggg.service.DoctorsService;
import truonggg.service.UserService;

@Transactional
@Service
@RequiredArgsConstructor
@Builder
public class DoctorsServiceIMPL implements DoctorsService {
	private final DoctorsRepository doctorsRepository;
	private final DoctorsMapper doctorsMapper;
	private final UserRepository userRepository;
	private final DepartmentsRepository departmentsRepository;
	private final UserService userService;

	@Override
	public List<DoctorsReponseDTO> getAll(Boolean featured) {
		List<Doctors> doctorsList;
		if (Boolean.TRUE.equals(featured)) {
			doctorsList = this.doctorsRepository.findTop5ByIsFeaturedTrueOrderByIdAsc();
		} else {
			doctorsList = this.doctorsRepository.findAll();
		}
		return doctorsMapper.toDTOList(doctorsList);
	}

	@Override
	public DoctorsReponseDTO createDoctor(DoctorsRequestDTO dto) {
		User user = this.userRepository.findById(dto.getUserId())
				.orElseThrow(() -> new NotFoundException("user", "User Not Found!"));
		Departments departments = this.departmentsRepository.findById(dto.getDepartmentId())
				.orElseThrow(() -> new NotFoundException("department", "Department Not Found!"));
		Doctors doctors = this.doctorsMapper.toEntity(dto);
		if (dto.getIsFeatured() == null) {
			doctors.setIsFeatured(false);
		} else {
			doctors.setIsFeatured(dto.getIsFeatured());
		}
		doctors.setUser(user);
		doctors.setDepartments(departments);
		doctors = this.doctorsRepository.save(doctors);
		return this.doctorsMapper.toDTO(doctors);
	}

	@Override
	public PagedResult<DoctorsReponseDTO> getDoctorsByDepartmentPaged(Integer departmentsId, Pageable pageable) {
		// Lấy page từ repository
		Page<Doctors> doctorsPage = doctorsRepository.findByDepartmentsId(departmentsId, pageable);

		// Chuyển đổi sang DTO
		List<DoctorsReponseDTO> dtoList = doctorsPage.stream().map(doctorsMapper::toDTO).collect(Collectors.toList());

		// Trả về PagedResult dùng builder
		PagedResult<DoctorsReponseDTO> pagedResult = PagedResult.<DoctorsReponseDTO>builder().content(dtoList)
				.totalElements((int) doctorsPage.getTotalElements()).totalPages(doctorsPage.getTotalPages())
				.currentPage(doctorsPage.getNumber()) // 0-based, cộng +1 nếu muốn 1-based
				.pageSize(doctorsPage.getSize()).build();

		return pagedResult;
	}

	@Override
	public PagedResult<DoctorsReponseDTO> getAllPaged(Pageable pageable) {
		// Lấy page từ repository - tất cả bác sĩ
		Page<Doctors> doctorsPage = doctorsRepository.findAll(pageable);

		// Chuyển đổi sang DTO
		List<DoctorsReponseDTO> dtoList = doctorsPage.stream().map(doctorsMapper::toDTO).collect(Collectors.toList());

		// Trả về PagedResult dùng builder
		PagedResult<DoctorsReponseDTO> pagedResult = PagedResult.<DoctorsReponseDTO>builder().content(dtoList)
				.totalElements((int) doctorsPage.getTotalElements()).totalPages(doctorsPage.getTotalPages())
				.currentPage(doctorsPage.getNumber()) // 0-based, cộng +1 nếu muốn 1-based
				.pageSize(doctorsPage.getSize()).build();

		return pagedResult;
	}

	@Override
	public DoctorsReponseDTO findById(Integer id) {
		Doctors doctors = this.doctorsRepository.findByIdWithSchedules(id)
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found!"));
		return this.doctorsMapper.toDTO(doctors);
	}

	@Override
	public DoctorsReponseDTO updateProfile(DoctorUpdateRequestDTO dto) {
		// Tìm doctor hiện tại
		Doctors foundDoctor = this.doctorsRepository.findById(dto.getId())
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));
		// Cập nhật thông tin User
		User user = foundDoctor.getUser();
		if (user != null) {
			if (dto.getFullName() != null) {
				user.setFullName(dto.getFullName());
			}
			if (dto.getEmail() != null) {
				user.setEmail(dto.getEmail());
			}
			if (dto.getPhone() != null) {
				user.setPhone(dto.getPhone());
			}
			if (dto.getAddress() != null) {
				user.setAddress(dto.getAddress());
			}
			if (dto.getDateOfBirth() != null) {
				user.setDateOfBirth(dto.getDateOfBirth());
			}
			// Lưu user đã cập nhật
			User updatedUser = this.userService.update(user);
			foundDoctor.setUser(updatedUser);
		}

		return this.doctorsMapper.toDTO(this.doctorsRepository.save(foundDoctor));
	}

	@Override
	public DoctorsReponseDTO updateWithUser(DoctorUpdateRequestDTO dto) {
		// Tìm doctor hiện tại
		Doctors foundDoctor = this.doctorsRepository.findById(dto.getId())
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));

		// Cập nhật thông tin Doctor
		if (dto.getExperienceYears() != null) {
			foundDoctor.setExperienceYears(dto.getExperienceYears());
		}
		if (dto.getDescription() != null) {
			foundDoctor.setDescription(dto.getDescription());
		}
		if (dto.getImageUrl() != null) {
			foundDoctor.setImageUrl(dto.getImageUrl());
		}
		if (dto.getIsFeatured() != null) {
			foundDoctor.setIsFeatured(dto.getIsFeatured());
		}

		// Cập nhật department nếu có
		if (dto.getDepartmentId() != null) {
			Departments departments = this.departmentsRepository.findById(dto.getDepartmentId())
					.orElseThrow(() -> new NotFoundException("department", "Department Not Found"));
			foundDoctor.setDepartments(departments);
		}

		// Cập nhật thông tin User
		User user = foundDoctor.getUser();
		if (user != null) {
			if (dto.getFullName() != null) {
				user.setFullName(dto.getFullName());
			}
			if (dto.getEmail() != null) {
				user.setEmail(dto.getEmail());
			}
			if (dto.getPhone() != null) {
				user.setPhone(dto.getPhone());
			}
			if (dto.getAddress() != null) {
				user.setAddress(dto.getAddress());
			}
			if (dto.getDateOfBirth() != null) {
				user.setDateOfBirth(dto.getDateOfBirth());
			}
			if (dto.getIsActive() != null) {
				user.setActive(dto.getIsActive());
			}
			// Lưu user đã cập nhật
			User updatedUser = this.userService.update(user);
			foundDoctor.setUser(updatedUser);
		}

		return this.doctorsMapper.toDTO(this.doctorsRepository.save(foundDoctor));
	}

	@Override
	public boolean delete(DoctorsDeleteRequestDTO dto) {
		// Tìm xem có bác sĩ không
		Doctors foundDoctor = this.doctorsRepository.findById(dto.getId())
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));

		// Soft delete - set isActive
		if (dto.getIsActive() != null) {
			foundDoctor.setIsActive(dto.getIsActive());
			this.doctorsRepository.save(foundDoctor);
		}
		return true;
	}

	@Override
	public boolean delete(Integer id) {
		// Tìm xem có bác sĩ không
		Doctors foundDoctor = this.doctorsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));

		// Hard delete - xóa hoàn toàn khỏi DB
		this.doctorsRepository.delete(foundDoctor);
		return true;
	}
}
