package ar.edu.iua.iw3.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
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
public class Orden {
    
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private int id;

    @Column
    private int estado;

    @Column(name = "camion")
    private Camion camion;

    @Column(name = "chofer")
    private Chofer chofer;

    @Column(name = "cliente")
    private Cliente cliente;

    @Column(name = "producto")
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


}
