package ar.edu.iua.iw3.controllers.dto;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CustomerDTO {
    private String id;
    private long socialNumber;
    private long phoneNumber;
    private String mail;
}