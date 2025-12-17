package ar.edu.iua.iw3.controllers.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderResponseDTO {
    private int id;
    private int state;
    private String externalCode;
    private TruckDTO truck;
    private DriverDTO driver;
    private CustomerDTO customer;
    private ProductDTO product;
    private LocalDateTime timeInitialReception;
    private Double preset;
    private Integer activationPassword;
    // ...otros campos de resumen
}