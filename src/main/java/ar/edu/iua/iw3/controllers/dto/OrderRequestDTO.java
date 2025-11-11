package ar.edu.iua.iw3.controllers.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequestDTO {
    private String externalCode;
    private TruckDTO truck;
    private DriverDTO driver;
    private CustomerDTO customer;
    private ProductDTO product;
    private LocalDateTime timeInitialReception;
    @PositiveOrZero
    private Double preset;
}