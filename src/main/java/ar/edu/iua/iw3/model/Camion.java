package ar.edu.iua.iw3.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "camiones")
@Inheritance(strategy = jakarta.persistence.InheritanceType.JOINED)
public class Camion {

    @Id
    private String codigoExterno;

    @Column (unique = true, nullable = false)
    private String patente;

    @Column
    private String descripcion;
    
    @Column
    private String cisternado;
}
