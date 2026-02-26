package truonggg.doctor.application.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import truonggg.Exception.NotFoundException;
import truonggg.appointment.domain.model.Appointments;
import truonggg.department.domain.model.Departments;
import truonggg.doctor.domain.model.Doctors;
import truonggg.schedules.domain.model.Schedules;
import truonggg.user.domain.model.User;
import truonggg.dto.reponseDTO.AppointmentsResponseDTO;
import truonggg.dto.reponseDTO.DoctorSummaryResponseDTO;
import truonggg.dto.reponseDTO.DoctorsReponseDTO;
import truonggg.dto.reponseDTO.SchedulesReponseDTO;
import truonggg.dto.requestDTO.DoctorUpdateRequestDTO;
import truonggg.dto.requestDTO.DoctorsDeleteRequestDTO;
import truonggg.dto.requestDTO.DoctorsRequestDTO;
import truonggg.appointment.mapper.AppointmentsMapper;
import truonggg.doctor.mapper.DoctorsMapper;
import truonggg.schedules.mapper.SchedulesMapper;
import truonggg.appointment.infrastructure.AppointmentsRepository;
import truonggg.department.infrastructure.DepartmentsRepository;
import truonggg.doctor.infrastructure.DoctorsRepository;
import truonggg.user.infrastructure.UserRepository;
import truonggg.schedules.infrastructure.SchedulesRepository;
import truonggg.reponse.PagedResult;
import truonggg.doctor.application.DoctorAdminService;
import truonggg.doctor.application.DoctorQueryService;
import truonggg.doctor.application.DoctorSelfService;

@Transactional
@Service
@RequiredArgsConstructor
@Builder
public class DoctorsServiceIMPL implements DoctorQueryService, DoctorAdminService, DoctorSelfService {
	private final DoctorsRepository doctorsRepository;
	private final DoctorsMapper doctorsMapper;
	private final UserRepository userRepository;
	private final DepartmentsRepository departmentsRepository;
	private final AppointmentsRepository appointmentsRepository;
	private final AppointmentsMapper appointmentsMapper;
	private final SchedulesRepository schedulesRepository;
	private final SchedulesMapper schedulesMapper;

	@Override
	public List<DoctorSummaryResponseDTO> getAll(Boolean featured) {
		List<Doctors> doctorsList;
		if (Boolean.TRUE.equals(featured)) {
			doctorsList = this.doctorsRepository.findTop5ByIsFeaturedTrueOrderByIdAsc();
		} else {
			doctorsList = this.doctorsRepository.findAll();
		}
		return doctorsMapper.toDTOOtherList(doctorsList);
	}

	@Override
	public DoctorSummaryResponseDTO createDoctor(DoctorsRequestDTO dto) {
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
		return this.doctorsMapper.toDTOOther(doctors);
	}

	@Override
	public PagedResult<DoctorSummaryResponseDTO> getDoctorsByDepartmentPaged(Integer departmentsId, Pageable pageable) {
		// Đảm bảo khoa tồn tại
		if (!this.departmentsRepository.existsById(departmentsId)) {
			throw new NotFoundException("department", "Department not found!");
		}
		// Lấy page từ repository
		Page<Doctors> doctorsPage = doctorsRepository.findByDepartmentsId(departmentsId, pageable);

		// Chuyển đổi sang DTO
		List<DoctorSummaryResponseDTO> dtoList = doctorsPage.stream().map(doctorsMapper::toDTOOther)
				.collect(Collectors.toList());

		return PagedResult.from(doctorsPage, dtoList);
	}

	@Override
	public PagedResult<DoctorSummaryResponseDTO> getAllPaged(Pageable pageable) {
		// Lấy page từ repository - tất cả bác sĩ
		Page<Doctors> doctorsPage = doctorsRepository.findAll(pageable);

		// Chuyển đổi sang DTO
		List<DoctorSummaryResponseDTO> dtoList = doctorsPage.stream().map(doctorsMapper::toDTOOther)
				.collect(Collectors.toList());

		return PagedResult.from(doctorsPage, dtoList);
	}

	@Override
	public DoctorsReponseDTO findById(Integer id) {
		Doctors doctors = this.doctorsRepository.findByIdWithSchedules(id)
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found!"));
		return this.doctorsMapper.toDTO(doctors);
	}

	@Override
	public DoctorSummaryResponseDTO updateProfile(DoctorUpdateRequestDTO dto, String userName) {

		Doctors foundDoctor = doctorsRepository.findByUser_UserName(userName)
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));

		if (dto.getExperienceYears() != null) {
			foundDoctor.setExperienceYears(dto.getExperienceYears());
		}
		if (dto.getDescription() != null) {
			foundDoctor.setDescription(dto.getDescription());
		}
		if (dto.getImageUrl() != null) {
			foundDoctor.setImageUrl(dto.getImageUrl());
		}
		if (dto.getDepartmentId() != null) {
			Departments departments = departmentsRepository.findById(dto.getDepartmentId())
					.orElseThrow(() -> new NotFoundException("department", "Department Not Found"));
			foundDoctor.setDepartments(departments);
		}

		// Cập nhật thông tin user đi kèm (SELF CONTEXT – KHÔNG ĐƯỢC SỬA isActive)
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
			// Không cho tự sửa isActive ở đây
			userRepository.save(user);
		}

		return doctorsMapper.toDTOOther(doctorsRepository.save(foundDoctor));
	}

	@Override
	public DoctorSummaryResponseDTO updateWithUser(Integer id, DoctorUpdateRequestDTO dto) {

		Doctors foundDoctor = this.doctorsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));

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
		if (dto.getDepartmentId() != null) {
			Departments departments = this.departmentsRepository.findById(dto.getDepartmentId())
					.orElseThrow(() -> new NotFoundException("department", "Department Not Found"));
			foundDoctor.setDepartments(departments);
		}
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
			//			if (dto.getIsActive() != null) {
			//				user.setActive(dto.getIsActive());
			//			}
			userRepository.save(user);
		}

		return this.doctorsMapper.toDTOOther(this.doctorsRepository.save(foundDoctor));
	}

	@Override
	public DoctorSummaryResponseDTO delete(Integer id, DoctorsDeleteRequestDTO dto) {
		// Tìm xem có bác sĩ không
		Doctors foundDoctor = this.doctorsRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("doctor", "Doctor Not Found"));

		// Soft delete - set isActive
		if (dto.getIsActive() != null) {
			foundDoctor.setIsActive(dto.getIsActive());
			this.doctorsRepository.save(foundDoctor);
		}
		return this.doctorsMapper.toDTOOther(foundDoctor);
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
	public PagedResult<DoctorSummaryResponseDTO> searchDoctors(String keyword, Pageable pageable) {
		Page<Doctors> doctorsPage = doctorsRepository.findByUserFullNameContainingIgnoreCase(keyword, pageable);

		// Chuyển đổi sang DTO
		List<DoctorSummaryResponseDTO> dtoList = doctorsPage.getContent().stream().map(doctorsMapper::toDTOOther)
				.collect(Collectors.toList());

		return PagedResult.from(doctorsPage, dtoList);
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
		Page<Appointments> appointmentsPage = this.appointmentsRepository.findByDoctors_Id(doctor.getId(), pageable);

		// Chuyển đổi sang DTO
		List<AppointmentsResponseDTO> dtoList = appointmentsPage.stream().map(appointmentsMapper::toDTO)
				.collect(Collectors.toList());

		return PagedResult.from(appointmentsPage, dtoList);
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
		Page<Schedules> schedulesPage = this.schedulesRepository.findByDoctorsId(doctor.getId(), pageable);

		List<SchedulesReponseDTO> dtoList = schedulesPage.stream().map(schedulesMapper::toDTO)
				.collect(Collectors.toList());

		return PagedResult.from(schedulesPage, dtoList);
	}
}


