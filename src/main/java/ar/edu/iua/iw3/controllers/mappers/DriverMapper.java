package ar.edu.iua.iw3.controllers.mappers;

import org.mapstruct.Mapper;
import ar.edu.iua.iw3.controllers.dto.DriverDTO;
import ar.edu.iua.iw3.model.Driver;

@Mapper(componentModel = "spring")
public interface DriverMapper {
    Driver toEntity(DriverDTO dto);
    DriverDTO toDto(Driver entity);
}