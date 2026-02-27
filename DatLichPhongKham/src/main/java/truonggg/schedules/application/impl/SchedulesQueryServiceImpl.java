package truonggg.schedules.application.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import truonggg.dto.reponseDTO.SchedulesReponseDTO;
import truonggg.reponse.PagedResult;
import truonggg.schedules.application.SchedulesQueryService;
import truonggg.schedules.domain.model.Schedules;
import truonggg.schedules.infrastructure.SchedulesRepository;
import truonggg.schedules.mapper.SchedulesMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchedulesQueryServiceImpl implements SchedulesQueryService {

    private final SchedulesRepository schedulesRepository;
    private final SchedulesMapper schedulesMapper;

    @Override
    public List<SchedulesReponseDTO> getAll() {
        List<Schedules> schedules = this.schedulesRepository.findAll();
        return this.schedulesMapper.toDTOList(schedules);
    }

    @Override
    public PagedResult<SchedulesReponseDTO> getAllPaged(Pageable pageable) {
        Page<Schedules> schedulesPage = this.schedulesRepository.findAll(pageable);
        List<SchedulesReponseDTO> dtoList = schedulesPage.stream().map(schedulesMapper::toDTO)
                .collect(Collectors.toList());

        return PagedResult.from(schedulesPage, dtoList);
    }

    @Override
    public PagedResult<SchedulesReponseDTO> getByDoctorId(Integer doctorId, Pageable pageable) {
        Page<Schedules> schedulesPage = this.schedulesRepository.findByDoctorsId(doctorId, pageable);
        List<SchedulesReponseDTO> dtoList = schedulesPage.stream()
                .map(schedulesMapper::toDTO)
                .collect(Collectors.toList());

        return PagedResult.from(schedulesPage, dtoList);
    }

}
