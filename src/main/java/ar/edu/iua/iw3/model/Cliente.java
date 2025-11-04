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

@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clientes")
@Inheritance(strategy = jakarta.persistence.InheritanceType.JOINED)
@Entity
@Setter
@Getter
public class Cliente {

    @Id
    private String codigoExterno;

    @Column
    private String razonSocial;

    @Column
    private int contacto;
    
}
