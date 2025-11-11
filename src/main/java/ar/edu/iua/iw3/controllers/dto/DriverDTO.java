package ar.edu.iua.iw3.controllers.dto;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DriverDTO {
    private String id;
    private String name;
    private String lastName;
    private long dni;
}