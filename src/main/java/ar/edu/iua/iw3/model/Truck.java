package ar.edu.iua.iw3.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Trucks")
@Inheritance(strategy = jakarta.persistence.InheritanceType.JOINED)
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class Truck {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column (unique = true, nullable = false)
    private String licensePlate;

    private String description;
    
    @OneToMany(mappedBy = "truck", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @JsonProperty("truncker")
    private List<Cistern> truncker = new ArrayList<>();
}
