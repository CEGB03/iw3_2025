package ar.edu.iua.iw3.controllers.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ar.edu.iua.iw3.controllers.dto.OrderDetailDTO;
import ar.edu.iua.iw3.model.OrderDetail;

@Mapper(componentModel = "spring")
public interface OrderDetailMapper {
    @org.mapstruct.Mapping(target = "id", ignore = true)
    @org.mapstruct.Mapping(target = "order", ignore = true)
    OrderDetail toEntity(OrderDetailDTO dto);
    OrderDetailDTO toDto(OrderDetail entity);
}