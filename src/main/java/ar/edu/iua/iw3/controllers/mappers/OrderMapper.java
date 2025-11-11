package ar.edu.iua.iw3.controllers.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ar.edu.iua.iw3.controllers.dto.OrderRequestDTO;
import ar.edu.iua.iw3.controllers.dto.OrderResponseDTO;
import ar.edu.iua.iw3.model.Order;

@Mapper(componentModel = "spring", uses = { TruckMapper.class, DriverMapper.class, CustomerMapper.class, ProductMapper.class })
public interface OrderMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "timeInitialWeighing", ignore = true)
    @Mapping(target = "timeInitialLoading", ignore = true)
    @Mapping(target = "timeFinalLoading", ignore = true)
    @Mapping(target = "timeFinalWeighing", ignore = true)
    @Mapping(target = "activationPassword", ignore = true)
    @Mapping(target = "initialWeighing", ignore = true)
    @Mapping(target = "finalWeighing", ignore = true)
    @Mapping(target = "lastMassAccumulated", ignore = true)
    @Mapping(target = "lastDensity", ignore = true)
    @Mapping(target = "lastTemperature", ignore = true)
    @Mapping(target = "lastFlow", ignore = true)
    @Mapping(target = "lastTimestamp", ignore = true)
    @Mapping(target = "details", ignore = true)
    Order toEntity(OrderRequestDTO dto);
    OrderResponseDTO toDto(Order order);

    // For updates: apply DTO fields into existing entity safely
    // void updateOrderFromDto(OrderRequestDTO dto, @MappingTarget Order entity);
}