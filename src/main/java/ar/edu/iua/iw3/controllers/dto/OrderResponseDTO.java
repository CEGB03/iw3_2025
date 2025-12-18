package ar.edu.iua.iw3.controllers.dto;

import java.time.LocalDateTime;
import java.util.List;
import ar.edu.iua.iw3.model.OrderDetail;
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
    private LocalDateTime timeInitialWeighing;
    private LocalDateTime timeInitialLoading;
    private LocalDateTime timeFinalLoading;
    private LocalDateTime timeFinalWeighing;
    private Double preset;
    private Integer activationPassword;
    private Double lastMassAccumulated;
    private Double lastDensity;
    private Double lastTemperature;
    private Double lastFlow;
    private LocalDateTime lastTimestamp;
    private List<OrderDetail> details;
}