package ar.edu.iua.iw3.controllers.mappers;

import org.mapstruct.Mapper;
import ar.edu.iua.iw3.controllers.dto.CustomerDTO;
import ar.edu.iua.iw3.model.Customer;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer toEntity(CustomerDTO dto);
    CustomerDTO toDto(Customer entity);
}