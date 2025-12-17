package ar.edu.iua.iw3.controllers.mappers;

import org.mapstruct.Mapper;
import ar.edu.iua.iw3.controllers.dto.TruckDTO;
import ar.edu.iua.iw3.model.Truck;

@Mapper(componentModel = "spring", uses = { CisternMapper.class })
public interface TruckMapper {
    Truck toEntity(TruckDTO dto);
    TruckDTO toDto(Truck entity);
}