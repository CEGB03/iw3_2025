package ar.edu.iua.iw3.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "Orders")
@Inheritance(strategy = jakarta.persistence.InheritanceType.JOINED)
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class Order {
    
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private int id;

    @Column (name = "States", columnDefinition = "tinyint default 1")
    private int state;

    @ManyToOne(cascade = {jakarta.persistence.CascadeType.PERSIST, jakarta.persistence.CascadeType.MERGE})
    private Truck truck;

    @ManyToOne(cascade = {jakarta.persistence.CascadeType.PERSIST, jakarta.persistence.CascadeType.MERGE})
    private Driver driver;

    @ManyToOne(cascade = {jakarta.persistence.CascadeType.PERSIST, jakarta.persistence.CascadeType.MERGE})
    private Customer customer;

    @ManyToOne(cascade = {jakarta.persistence.CascadeType.PERSIST, jakarta.persistence.CascadeType.MERGE})
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

    // External code coming from external systems (flexible text)
    @Column(name = "external_code")
    private String externalCode;

    // Activation password (5 digits) generated when initial weighing is registered
    @Column(name = "activation_password")
    private Integer activationPassword;

    // Initial (tara) and final weighing
    @Column(name = "initial_weighing")
    private Double initialWeighing;

    @Column(name = "final_weighing")
    private Double finalWeighing;

    // Last values received during loading (cabecera)
    @Column(name = "last_mass_accumulated")
    private Double lastMassAccumulated;

    @Column(name = "last_density")
    private Double lastDensity;

    @Column(name = "last_temperature")
    private Double lastTemperature;

    @Column(name = "last_flow")
    private Double lastFlow;

    @Column(name = "last_timestamp")
    private LocalDateTime lastTimestamp;

    @OneToMany(mappedBy = "order", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> details = new ArrayList<>();


}
