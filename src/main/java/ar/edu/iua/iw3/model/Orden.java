package ar.edu.iua.iw3.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.ManyToAny;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ordenes")
@Inheritance(strategy = jakarta.persistence.InheritanceType.JOINED)
@Getter
@Setter
@Entity
public class Orden {
    
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private int id;

    @Column (name = "estado")
    private int estado;

    @ManyToMany
    private Camion camion;

    @ManyToMany
    private Chofer chofer;

    @ManyToAny
    private Cliente cliente;

    @ManyToOne
    private Producto producto;

    @Column(name = "fyh_recepcion_inicial")
    private LocalDateTime fyhRecepcionInicial;

    @Column(name = "fyh_pesaje_inicial")
    private LocalDateTime fyhPesajeInicial;

    @Column(name = "fyh_inicio_carga")
    private LocalDateTime fyhInicioCarga;

    @Column(name = "fyh_fin_carga")
    private LocalDateTime fyhFinCarga;

    @Column(name = "fyh_pesaje_final")
    private LocalDateTime fyhPesajeFinal;

    @Column(name = "preset")
    private double preset;


}
