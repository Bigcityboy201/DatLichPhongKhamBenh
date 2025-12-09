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
import truonggg.dto.reponseDTO.AppointmentsResponseDTO;
import truonggg.dto.reponseDTO.DoctorsReponseDTO;
import truonggg.dto.reponseDTO.SchedulesReponseDTO;
import truonggg.dto.requestDTO.DoctorUpdateRequestDTO;
import truonggg.dto.requestDTO.DoctorsDeleteRequestDTO;
import truonggg.dto.requestDTO.DoctorsRequestDTO;
import truonggg.mapper.AppointmentsMapper;
import truonggg.mapper.DoctorsMapper;
import truonggg.mapper.SchedulesMapper;
import truonggg.repo.AppointmentsRepository;
import truonggg.repo.DepartmentsRepository;
import truonggg.repo.DoctorsRepository;
import truonggg.repo.SchedulesRepository;
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
	private final AppointmentsRepository appointmentsRepository;
	private final AppointmentsMapper appointmentsMapper;
	private final SchedulesRepository schedulesRepository;
	private final SchedulesMapper schedulesMapper;

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
		Departments departments = this.departmentsRepository.findById(departmentsId)
				.orElseThrow(() -> new NotFoundException("department", "Department not found!"));
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
	public DoctorsReponseDTO updateProfile(Integer id, DoctorUpdateRequestDTO dto, String userName) {
		// Tìm doctor hiện tại
		Doctors foundDoctor = this.doctorsRepository.findById(id)
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
			if (dto.getExperienceYears() != null) {
				foundDoctor.setExperienceYears(dto.getExperienceYears());
			}
			if (dto.getDescription() != null) {
				foundDoctor.setDescription(dto.getDescription());
			}
			if (dto.getImageUrl() != null) {
				foundDoctor.setImageUrl(dto.getImageUrl());
			}

			// Cập nhật department nếu có
			if (dto.getDepartmentId() != null) {
				Departments departments = this.departmentsRepository.findById(dto.getDepartmentId())
						.orElseThrow(() -> new NotFoundException("department", "Department Not Found"));
				foundDoctor.setDepartments(departments);
			}
			// Lưu user đã cập nhật
			User updatedUser = this.userService.update(user);
			foundDoctor.setUser(updatedUser);
		}

		return this.doctorsMapper.toDTO(this.doctorsRepository.save(foundDoctor));
	}

	@Override
	public DoctorsReponseDTO updateWithUser(Integer id, DoctorUpdateRequestDTO dto) {
		// Tìm doctor hiện tại
		Doctors foundDoctor = this.doctorsRepository.findById(id)
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
	public DoctorsReponseDTO delete(Integer id, DoctorsDeleteRequestDTO dto) {
		// Tìm xem có bác sĩ không
		Doctors foundDoctor = this.doctorsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));

		// Soft delete - set isActive
		if (dto.getIsActive() != null) {
			foundDoctor.setIsActive(dto.getIsActive());
			this.doctorsRepository.save(foundDoctor);
		}
		return this.doctorsMapper.toDTO(foundDoctor);
	}

	@Override
	public boolean deleteManually(Integer id) {
		// Tìm xem có bác sĩ không
		Doctors foundDoctor = this.doctorsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));

		User user = foundDoctor.getUser();
		if (user != null) {
			user.setDoctors(null);
			userRepository.delete(user);
		}
		// Hard delete - xóa hoàn toàn khỏi DB
		this.doctorsRepository.delete(foundDoctor);
		return true;
	}

	@Override
	public PagedResult<DoctorsReponseDTO> searchDoctors(String keyword, Pageable pageable) {
		Page<Doctors> doctorsPage = doctorsRepository.findByUserFullNameContainingIgnoreCase(keyword, pageable);

		// Chuyển đổi sang DTO
		List<DoctorsReponseDTO> dtoList = doctorsPage.getContent().stream().map(doctorsMapper::toDTO)
				.collect(Collectors.toList());

		// Trả về PagedResult dùng builder
		PagedResult<DoctorsReponseDTO> pagedResult = PagedResult.<DoctorsReponseDTO>builder().content(dtoList)
				.totalElements((int) doctorsPage.getTotalElements()).totalPages(doctorsPage.getTotalPages())
				.currentPage(doctorsPage.getNumber()) // 0-based, cộng +1 nếu muốn 1-based
				.pageSize(doctorsPage.getSize()).build();

		return pagedResult;
	}

	@Override
	public DoctorsReponseDTO findByUserName(String userName) {
		// Tìm user theo username
		User user = this.userRepository.findByUserName(userName)
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		// Tìm doctor theo user
		Doctors doctor = this.doctorsRepository.findByUser(user)
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found for this user"));

		// Lấy doctor với schedules
		doctor = this.doctorsRepository.findByIdWithSchedules(doctor.getId())
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));

		return this.doctorsMapper.toDTO(doctor);
	}

	@Override
	public PagedResult<AppointmentsResponseDTO> getMyAppointments(String userName, Pageable pageable) {
		// Tìm user theo username
		User user = this.userRepository.findByUserName(userName)
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		// Tìm doctor theo user
		Doctors doctor = this.doctorsRepository.findByUser(user)
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found for this user"));

		// Lấy appointments theo doctor với phân trang
		Page<truonggg.Model.Appointments> appointmentsPage = this.appointmentsRepository
				.findByDoctors_Id(doctor.getId(), pageable);

		// Chuyển đổi sang DTO
		List<AppointmentsResponseDTO> dtoList = appointmentsPage.stream().map(appointmentsMapper::toDTO)
				.collect(Collectors.toList());

		// Trả về PagedResult
		PagedResult<AppointmentsResponseDTO> pagedResult = PagedResult.<AppointmentsResponseDTO>builder()
				.content(dtoList).totalElements((int) appointmentsPage.getTotalElements())
				.totalPages(appointmentsPage.getTotalPages()).currentPage(appointmentsPage.getNumber())
				.pageSize(appointmentsPage.getSize()).build();

		return pagedResult;
	}

	@Override
	public PagedResult<SchedulesReponseDTO> getMySchedules(String userName, Pageable pageable) {
		// Tìm user theo username
		User user = this.userRepository.findByUserName(userName)
				.orElseThrow(() -> new NotFoundException("user", "User Not Found"));

		// Tìm doctor theo user
		Doctors doctor = this.doctorsRepository.findByUser(user)
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found for this user"));

		// Lấy schedules theo doctor với phân trang
		Page<truonggg.Model.Schedules> schedulesPage = this.schedulesRepository.findByDoctorsId(doctor.getId(),
				pageable);

		List<SchedulesReponseDTO> dtoList = schedulesPage.stream().map(schedulesMapper::toDTO)
				.collect(Collectors.toList());

		return PagedResult.<SchedulesReponseDTO>builder().content(dtoList)
				.totalElements((int) schedulesPage.getTotalElements()).totalPages(schedulesPage.getTotalPages())
				.currentPage(schedulesPage.getNumber()).pageSize(schedulesPage.getSize()).build();
	}
}
