package ar.edu.iua.iw3.controllers.dto;
import lombok.Getter; 
import lombok.Setter;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter @Setter
public class CisternDTO {
    private Integer id;
    private Double capacity;
    @JsonProperty("licence_plate")
    private String licencePlate;
}