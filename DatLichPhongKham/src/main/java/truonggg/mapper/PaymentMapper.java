package truonggg.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import truonggg.Model.Payments;
import truonggg.dto.reponseDTO.PaymentResponseDTO;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
	
	@Mapping(source = "appointments.id", target = "appointmentId")
	@Mapping(source = "gatewayTransactionNo", target = "gatewayTransactionNo")
	@Mapping(source = "responseCode", target = "responseCode")
	PaymentResponseDTO toDTO(Payments payment);
	
	default List<PaymentResponseDTO> toDTOList(List<Payments> payments) {
		if (payments == null || payments.isEmpty())
			return List.of();
		return payments.stream().map(this::toDTO).collect(Collectors.toList());
	}
}

