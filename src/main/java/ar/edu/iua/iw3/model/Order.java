package ar.edu.iua.iw3.model;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Orders")
@Inheritance(strategy = jakarta.persistence.InheritanceType.JOINED)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Order {
    
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private int id;

    @Column (name = "States")
    private int state;

    @ManyToOne (cascade = CascadeType.ALL)
    private Truck truck;

    @ManyToOne (cascade = CascadeType.ALL)
    private Driver driver;

    @ManyToOne(cascade = CascadeType.ALL)
    private Customer customer;

    @ManyToOne(cascade = CascadeType.ALL)
    private Product product;

    @Column(name = "Time Initial Reception")
    private LocalDateTime timeInitialReception;

    @Column(name = "Time Initial Weighing")
    private LocalDateTime timeInitialWeighing;

    @Column(name = "Time Initial Loading")
    private LocalDateTime timeInitialLoading;

    @Column(name = "Time Final Loading")
    private LocalDateTime timeFinalLoading;

    @Column(name = "Time Final Weighing")
    private LocalDateTime timeFinalWeighing;

    //@Min (tiene que ser mayor a cero si o si) algo de jakarta.validation pero no aparece
    @Column(name = "preset")
    private double preset;


}
