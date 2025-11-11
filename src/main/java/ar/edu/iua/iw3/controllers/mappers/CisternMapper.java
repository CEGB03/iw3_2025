package ar.edu.iua.iw3.controllers.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ar.edu.iua.iw3.controllers.dto.CisternDTO;
import ar.edu.iua.iw3.model.Cistern;

@Mapper(componentModel = "spring")
public interface CisternMapper {
    @org.mapstruct.Mapping(target = "truck", ignore = true)
    Cistern toEntity(CisternDTO dto);
    CisternDTO toDto(Cistern entity);
}