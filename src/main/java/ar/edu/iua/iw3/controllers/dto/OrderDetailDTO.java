package ar.edu.iua.iw3.controllers.dto;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrderDetailDTO {
    private Double massAccumulated;
    private Double density;
    private Double temperature;
    private Double flow;
    private LocalDateTime timeStamp;
}