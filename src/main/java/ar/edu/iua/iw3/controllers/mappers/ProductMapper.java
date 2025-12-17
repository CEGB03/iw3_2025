package ar.edu.iua.iw3.controllers.mappers;

import org.mapstruct.Mapper;
import ar.edu.iua.iw3.controllers.dto.ProductDTO;
import ar.edu.iua.iw3.model.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toEntity(ProductDTO dto);
    ProductDTO toDto(Product entity);
}