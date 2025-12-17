package ar.edu.iua.iw3.controllers.dto;
import lombok.Getter; 
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class TruckDTO {
    private String id;
    private String licensePlate;
    private String description;
    private List<CisternDTO> truncker; // coincide con JSON "truncker"
}