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
@Table(name = "productos")
@Inheritance(strategy = jakarta.persistence.InheritanceType.JOINED)
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Producto {
    
    @Id
    private String codigoExterno;

    @Column (unique = true, nullable = false)
    private String nombre;

    @Column
    private String descripcion;
}
