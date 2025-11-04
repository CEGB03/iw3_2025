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
@Setter
@Getter
@Table(name = "choferes")
@Inheritance(strategy = jakarta.persistence.InheritanceType.JOINED)
public class Chofer {
    
    @Id
    private String codigoExterno;

    @Column
    private String nombre;

    @Column
    private String apellido;

    @Column
    private int documento;
}
