package ar.edu.iua.iw3.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty; // <- AGREGAR ESTE IMPORT

@Entity
@Table(name = "Order_Details")
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private int id;

    @Column(name = "mass_accumulated")
    @JsonProperty("mass_accumulated") // <- AGREGAR ESTO
    private Double massAccumulated;

    @Column(name = "density")
    @JsonProperty("density") // <- AGREGAR ESTO
    private Double density;

    @Column(name = "temperature")
    @JsonProperty("temperature") // <- AGREGAR ESTO
    private Double temperature;

    @Column(name = "flow")
    @JsonProperty("flow") // <- AGREGAR ESTO
    private Double flow;

    @Column(name = "time_stamp")
    @JsonProperty("time_stamp") // <- AGREGAR ESTO (opcional si usas timeStamp en JSON)
    private LocalDateTime timeStamp;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonBackReference
    private Order order;

}
