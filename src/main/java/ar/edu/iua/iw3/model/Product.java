package ar.edu.iua.iw3.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "Products")
@Inheritance(strategy = jakarta.persistence.InheritanceType.JOINED)
@NoArgsConstructor
@Setter
@Getter
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class Product {
    
    @Id
    //@GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private String id;

    //Hay que poner para que nunca esté vacío con @NotEmpty, creo que tmb es de jakarta.validation
    @Column (unique = true, nullable = false)
    private String productName;

    @Column
    private String description;
}
